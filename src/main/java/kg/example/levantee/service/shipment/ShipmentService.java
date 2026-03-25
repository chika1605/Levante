package kg.example.levantee.service.shipment;

import jakarta.transaction.Transactional;
import kg.example.levantee.dto.cdekDto.CdekCreateOrderRequest;
import kg.example.levantee.dto.cdekDto.CdekCreateOrderResponse;
import kg.example.levantee.dto.shipmentDto.ShipmentCreateResponse;
import kg.example.levantee.dto.shipmentDto.ShipmentPackage;
import kg.example.levantee.dto.shipmentDto.ShipmentParams;
import kg.example.levantee.dto.shipmentDto.ShipmentRequest;
import kg.example.levantee.dto.shipmentDto.ShipmentResponse;
import kg.example.levantee.dto.shipmentDto.TariffInfo;
import kg.example.levantee.model.entity.order.Order;
import kg.example.levantee.model.entity.shipment.Shipment;
import kg.example.levantee.model.enums.order.OrderStatus;
import kg.example.levantee.repository.OrderRepository;
import kg.example.levantee.repository.ShipmentRepository;
import kg.example.levantee.service.shipment.cdek.CdekClient;
import kg.example.levantee.service.shipment.cdek.CdekProperties;
import kg.example.levantee.service.shipment.cdek.CdekService;
import kg.example.levantee.utils.exception.NotFoundException;
import kg.example.levantee.utils.exception.PriceChangedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShippingStrategyFactory shippingStrategyFactory;
    private final ShipmentPriceCacheService priceCacheService;
    private final CdekService cdekService;
    private final CdekClient cdekClient;
    private final CdekProperties cdekProperties;

    // ─── GET /shipment/tariffs ─────────────────────────────────────────────────

    public List<TariffInfo> getTariffs(int toCityCode) {
        return shippingStrategyFactory.getAll().stream()
                .flatMap(strategy -> {
                    try {
                        return strategy.getTariffs(toCityCode).stream();
                    } catch (Exception e) {
                        log.warn("Не удалось получить тарифы от {}: {}", strategy.getCarrierName(), e.getMessage());
                        return Stream.empty();
                    }
                })
                .toList();
    }

    // ─── GET /shipment/calculate ───────────────────────────────────────────────

    public ShipmentResponse calculate(Long orderId, String tariffId, boolean insuranceEnabled) {
        String[] parts = parseTariffId(tariffId);
        String carrier = parts[0];
        int tariffCode = Integer.parseInt(parts[1]);
        int toCityCode = Integer.parseInt(parts[2]);

        if (!orderRepository.existsById(orderId)) throw new NotFoundException("Заказ не найден");

        Double totalAmount = orderRepository.findTotalAmountByOrderId(orderId);
        if (totalAmount == null || totalAmount <= 0) throw new IllegalArgumentException("Сумма заказа должна быть больше 0");

        List<ShipmentPackage> packages = getPackages(orderId);
        double deliveryPrice  = shippingStrategyFactory.get(carrier).calculate(packages, new ShipmentParams(0, toCityCode, tariffCode));
        double insurancePrice = insuranceEnabled ? Math.round(totalAmount * 0.01 * 100.0) / 100.0 : 0.0;
        double totalPrice     = deliveryPrice + insurancePrice;

        priceCacheService.save(orderId, deliveryPrice, insurancePrice, totalPrice, totalAmount, insuranceEnabled, tariffCode);
        log.info("Цена рассчитана orderId={}: доставка={}, страховка={}, итого={}", orderId, deliveryPrice, insurancePrice, totalPrice);

        return new ShipmentResponse(deliveryPrice, insurancePrice, totalPrice, totalAmount, insuranceEnabled);
    }

    // ─── POST /shipment ────────────────────────────────────────────────────────

    @Transactional
    public ShipmentCreateResponse createShipment(ShipmentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        if (order.getStatus() != OrderStatus.CART) {
            throw new IllegalStateException("Оформить доставку можно только для заказа в статусе CART");
        }

        String[] parts = parseTariffId(request.getTariffId());
        String carrier = parts[0];
        int tariffCode = Integer.parseInt(parts[1]);
        int toCityCode = Integer.parseInt(parts[2]);

        List<ShipmentPackage> packages = getPackages(request.getOrderId());

        // 1. Свежая цена
        double freshDelivery  = shippingStrategyFactory.get(carrier).calculate(packages, new ShipmentParams(0, toCityCode, tariffCode));
        ShipmentPriceCacheService.Entry cached = priceCacheService.get(request.getOrderId());
        boolean insuranceEnabled = cached != null && cached.isInsuranceEnabled();
        Double totalAmount = orderRepository.findTotalAmountByOrderId(request.getOrderId());
        double declaredValue  = (insuranceEnabled && totalAmount != null) ? totalAmount : 0.0;
        double freshInsurance = insuranceEnabled ? Math.round(declaredValue * 0.01 * 100.0) / 100.0 : 0.0;
        double freshTotal     = freshDelivery + freshInsurance;

        // 2. Сравниваем с кэшированной ценой из Redis
        if (cached != null && Math.abs(cached.getTotalPrice() - freshTotal) > 0.01) {
            priceCacheService.save(request.getOrderId(), freshDelivery, freshInsurance,
                    freshTotal, declaredValue, insuranceEnabled, tariffCode);
            throw new PriceChangedException(cached.getTotalPrice(), freshTotal);
        }

        // 4. Регистрируем в СДЭК
        CdekCreateOrderRequest cdekReq = buildCdekRequest(request, packages, tariffCode, toCityCode, freshInsurance);
        CdekCreateOrderResponse cdekResponse = cdekService.createOrder(cdekReq);

        // 5. Сохраняем Shipment
        Shipment shipment = Shipment.builder()
                .order(order)
                .carrier(carrier)
                .tariffId(request.getTariffId())
                .tariffCode(tariffCode)
                .tariffName(request.getTariffName())
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryPrice(freshDelivery)
                .insurancePrice(freshInsurance)
                .calculatedPrice(freshTotal)
                .declaredValue(declaredValue)
                .cdekUuid(cdekResponse.getUuid())
                .cdekRequestStatus(cdekResponse.getStatus())
                .cdekPollAttempts(0)
                .build();

        shipmentRepository.save(shipment);

        // 6. Статус заказа → COMPLETED
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        // 7. Удаляем ключ из Redis
        priceCacheService.delete(request.getOrderId());

        log.info("Доставка оформлена: shipment={}, cdekUuid={}, price={}", shipment.getId(), cdekResponse.getUuid(), freshTotal);

        return new ShipmentCreateResponse(shipment.getId(), order.getId(), carrier,
                request.getTariffId(), order.getStatus(),
                cdekResponse.getUuid(), cdekResponse.getStatus(), freshTotal);
    }

    // ─── DELETE /shipment/:cdekUuid ───────────────────────────────────────────

    @Transactional
    public void cancelShipment(String cdekUuid) {
        Shipment shipment = shipmentRepository.findByCdekUuid(cdekUuid)
                .orElseThrow(() -> new NotFoundException("Доставка не найдена: " + cdekUuid));

        if (!"Создан".equals(shipment.getCdekStatus())) {
            throw new IllegalArgumentException(
                    "Отмена возможна только для статуса 'Создан'. Текущий: " + shipment.getCdekStatus());
        }

        cdekClient.cancelOrder(cdekUuid);

        shipment.getOrder().setStatus(OrderStatus.CANCELLED);
        orderRepository.save(shipment.getOrder());

        log.info("Заказ cdekUuid={} отменён", cdekUuid);
    }

    // ─── POST /shipment/:cdekUuid/refusal ─────────────────────────────────────

    public void refuseShipment(String cdekUuid) {
        Shipment shipment = shipmentRepository.findByCdekUuid(cdekUuid)
                .orElseThrow(() -> new NotFoundException("Доставка не найдена: " + cdekUuid));

        if ("Создан".equals(shipment.getCdekStatus())) {
            throw new IllegalArgumentException(
                    "Для статуса 'Создан' используйте DELETE /shipment/{cdekUuid}");
        }

        cdekClient.refuseOrder(cdekUuid);
        log.info("Отказ от заказа cdekUuid={} зарегистрирован", cdekUuid);
    }

    // ─── POST /shipment/:cdekUuid/return ──────────────────────────────────────

    public void returnShipment(String cdekUuid, int returnTariffCode) {
        Shipment shipment = shipmentRepository.findByCdekUuid(cdekUuid)
                .orElseThrow(() -> new NotFoundException("Доставка не найдена: " + cdekUuid));

        if (!"Вручен".equals(shipment.getCdekStatus())) {
            throw new IllegalArgumentException(
                    "Возврат возможен только для статуса 'Вручен'. Текущий: " + shipment.getCdekStatus());
        }

        cdekClient.clientReturn(cdekUuid, returnTariffCode);
        log.info("Возврат cdekUuid={} инициирован", cdekUuid);
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private String[] parseTariffId(String tariffId) {
        String[] parts = tariffId.split(":");
        if (parts.length != 3) throw new IllegalArgumentException("Неверный формат tariffId. Ожидается: CARRIER:tariffCode:toCityCode");
        return parts;
    }

    private List<ShipmentPackage> getPackages(Long orderId) {
        List<ShipmentPackage> packages = orderRepository.findShipmentPackages(orderId).stream()
                .map(p -> new ShipmentPackage(p.getWeightKg(), p.getLengthCm(), p.getWidthCm(), p.getHeightCm(), p.getQuantity()))
                .toList();
        if (packages.isEmpty()) throw new IllegalStateException("Заказ не содержит товаров");
        return packages;
    }

    private CdekCreateOrderRequest buildCdekRequest(ShipmentRequest req, List<ShipmentPackage> packages,
                                                     int tariffCode, int toCityCode, double insuranceAmount) {
        boolean isPvz = req.getDeliveryAddress() != null
                && !req.getDeliveryAddress().isBlank()
                && !req.getDeliveryAddress().contains(" ");  // ПВЗ — короткий код без пробелов

        CdekCreateOrderRequest r = new CdekCreateOrderRequest();
        r.setOrderId(req.getOrderId());
        r.setTariffCode(tariffCode);
        r.setFromCityCode(cdekProperties.getFromCityCode());
        r.setToCityCode(toCityCode);
        r.setRecipientName(req.getRecipientName());
        r.setRecipientPhone(req.getRecipientPhone());
        r.setRecipientEmail(req.getRecipientEmail());
        r.setPackages(packages);
        r.setInsuranceAmount(insuranceAmount > 0 ? insuranceAmount : null);

        if (isPvz) {
            r.setDeliveryPoint(req.getDeliveryAddress());
        } else {
            r.setToAddress(req.getDeliveryAddress());
        }
        return r;
    }
}
