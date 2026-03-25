package kg.example.levantee.service.shipment;

import jakarta.transaction.Transactional;
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
import kg.example.levantee.utils.exception.NotFoundException;
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

    // POST /shipment — сохраняет доставку и меняет статус CART → PENDING
    @Transactional
    public ShipmentCreateResponse createShipment(ShipmentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        if (order.getStatus() != OrderStatus.CART) {
            throw new IllegalStateException("Оформить доставку можно только для заказа в статусе CART");
        }

        String[] parts = request.getTariffId().split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Неверный формат tariffId. Ожидается: CARRIER:tariffCode:toCityCode");
        }

        Shipment shipment = Shipment.builder()
                .order(order)
                .carrier(parts[0])
                .tariffId(request.getTariffId())
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .deliveryAddress(request.getDeliveryAddress())
                .build();

        shipmentRepository.save(shipment);

        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        log.info("Доставка оформлена для заказа #{}, статус → PENDING", order.getId());

        return new ShipmentCreateResponse(
                shipment.getId(),
                order.getId(),
                shipment.getCarrier(),
                shipment.getTariffId(),
                order.getStatus()
        );
    }

    // GET /shipment/tariffs?toCityCode=270
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

    // GET /shipment/calculate?orderId=1&tariffId=CDEK:136:270
    public ShipmentResponse calculate(Long orderId, String tariffId) {
        String[] parts = tariffId.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Неверный формат tariffId. Ожидается: CARRIER:tariffCode:toCityCode");
        }

        String carrier = parts[0];
        int tariffCode = Integer.parseInt(parts[1]);
        int toCityCode = Integer.parseInt(parts[2]);

        if (!orderRepository.existsById(orderId)) {
            throw new NotFoundException("Заказ не найден");
        }

        List<ShipmentPackage> packages = orderRepository.findShipmentPackages(orderId).stream()
                .map(p -> new ShipmentPackage(p.getWeightKg(), p.getLengthCm(), p.getWidthCm(), p.getHeightCm(), p.getQuantity()))
                .toList();
        if (packages.isEmpty()) {
            throw new IllegalStateException("Заказ не содержит товаров");
        }

        double totalWeight = packages.stream()
                .mapToDouble(p -> p.getWeightKg() * p.getQuantity())
                .sum();

        ShipmentParams params = new ShipmentParams(0, toCityCode, tariffCode);
        double price = shippingStrategyFactory.get(carrier).calculate(packages, params);

        return new ShipmentResponse(carrier, totalWeight, price);
    }
}