package kg.example.levantee.service.shipment;

import jakarta.transaction.Transactional;
import kg.example.levantee.dto.cdekDto.CdekCreateOrderRequest;
import kg.example.levantee.dto.cdekDto.CdekCreateOrderResponse;
import kg.example.levantee.dto.mapper.ShipmentMapper;
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
import kg.example.levantee.service.shipment.cdek.model.CdekProperties;
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
    private final CdekProperties cdekProperties;
    private final ShipmentMapper shipmentMapper;


    public List<TariffInfo> getTariffs(int toCityCode, Long orderId) {
        List<ShipmentPackage> packages = orderId != null ? getPackages(orderId) : List.of();
        return shippingStrategyFactory.getAll().stream()
                .flatMap(strategy -> {
                    try {
                        return strategy.getTariffs(toCityCode, packages).stream();
                    } catch (Exception e) {
                        log.warn("Не удалось получить тарифы от {}: {}", strategy.getCarrierName(), e.getMessage());
                        return Stream.empty();
                    }
                })
                .toList();
    }


    public ShipmentResponse calculate(Long orderId, String tariffId, boolean insuranceEnabled) {
        String[] parts = parseTariffId(tariffId);
        String carrier = parts[0];
        int tariffCode = Integer.parseInt(parts[1]);
        int toCityCode = Integer.parseInt(parts[2]);

        if (!orderRepository.existsById(orderId)) throw new NotFoundException("Заказ не найден");

        Double totalAmount = orderRepository.findTotalAmountByOrderId(orderId);
        if (totalAmount == null || totalAmount <= 0)
            throw new IllegalArgumentException("Сумма заказа должна быть больше 0");

        List<ShipmentPackage> packages = getPackages(orderId);
        double deliveryPrice = shippingStrategyFactory.get(carrier).calculate(packages, new ShipmentParams(0, toCityCode, tariffCode));
        double insurancePrice = insuranceEnabled ? Math.round(totalAmount * 0.01 * 100.0) / 100.0 : 0.0;
        double totalPrice = deliveryPrice + insurancePrice;

        priceCacheService.save(orderId, deliveryPrice, insurancePrice, totalPrice, totalAmount, insuranceEnabled, tariffCode);
        log.info("Цена рассчитана orderId={}: доставка={}, страховка={}, итого={}", orderId, deliveryPrice, insurancePrice, totalPrice);

        double grandTotal = Math.round((totalAmount != null ? totalAmount + totalPrice : totalPrice) * 100.0) / 100.0;
        return new ShipmentResponse(deliveryPrice, insurancePrice, totalPrice, totalAmount, insuranceEnabled, grandTotal);
    }


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

        double freshDelivery = shippingStrategyFactory.get(carrier).calculate(packages, new ShipmentParams(0, toCityCode, tariffCode));
        ShipmentPriceCacheService.Entry cached = priceCacheService.get(request.getOrderId());
        boolean insuranceEnabled = cached != null && cached.isInsuranceEnabled();
        Double totalAmount = orderRepository.findTotalAmountByOrderId(request.getOrderId());
        double declaredValue = (insuranceEnabled && totalAmount != null) ? totalAmount : 0.0;
        double freshInsurance = insuranceEnabled ? Math.round(declaredValue * 0.01 * 100.0) / 100.0 : 0.0;
        double freshTotal = freshDelivery + freshInsurance;

        if (cached != null && Math.abs(cached.getTotalPrice() - freshTotal) > 0.01) {
            priceCacheService.save(request.getOrderId(), freshDelivery, freshInsurance, freshTotal, declaredValue, insuranceEnabled, tariffCode);
            throw new PriceChangedException(cached.getTotalPrice(), freshTotal);
        }

        Shipment shipment;
        String cdekUuid = null;
        String cdekStatus = null;

        if ("CDEK".equals(carrier)) {
            CdekCreateOrderRequest cdekRequest = shipmentMapper.toCdekRequest(request, packages, tariffCode, toCityCode, freshInsurance, cdekProperties.getFromCityCode());
            CdekCreateOrderResponse cdekResponse = cdekService.createOrder(cdekRequest);
            cdekUuid = cdekResponse.getUuid();
            cdekStatus = cdekResponse.getStatus();
            shipment = shipmentMapper.toEntity(order, request, carrier, tariffCode, freshDelivery, freshInsurance, freshTotal, declaredValue, cdekResponse);
        } else {
            shipment = shipmentMapper.toEntity(order, request, carrier, tariffCode, freshDelivery, freshInsurance, freshTotal, declaredValue, null);
        }

        shipmentRepository.save(shipment);

        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        priceCacheService.delete(request.getOrderId());

        log.info("Доставка оформлена: shipmentId={}, carrier={}, cdekUuid={}, price={}", shipment.getId(), carrier, cdekUuid, freshTotal);

        return shipmentMapper.toCreateResponse(shipment, cdekUuid, cdekStatus, freshTotal);
    }

    private String[] parseTariffId(String tariffId) {
        String[] parts = tariffId.split(":");
        if (parts.length != 3)
            throw new IllegalArgumentException("Неверный формат tariffId. Ожидается: CARRIER:tariffCode:toCityCode");
        return parts;
    }

    private List<ShipmentPackage> getPackages(Long orderId) {
        List<ShipmentPackage> packages = orderRepository.findShipmentPackages(orderId).stream()
                .map(p -> new ShipmentPackage(p.getWeightKg(), p.getLengthCm(), p.getWidthCm(), p.getHeightCm(), p.getQuantity()))
                .toList();
        if (packages.isEmpty()) throw new IllegalStateException("Заказ не содержит товаров");
        return packages;
    }
}