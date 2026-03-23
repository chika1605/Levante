package kg.example.levantee.service.shipment;

import kg.example.levantee.dto.shipmentDto.ShipmentResponse;
import kg.example.levantee.repository.OrderRepository;
import kg.example.levantee.utils.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final OrderRepository orderRepository;
    private final ShippingStrategyFactory shippingStrategyFactory;

    public List<ShipmentResponse> calculateAll(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new NotFoundException("Заказ не найден");
        }

        Double totalWeight = orderRepository.calculateTotalWeight(orderId);
        if (totalWeight == null || totalWeight == 0) {
            throw new IllegalStateException("Заказ не содержит товаров с указанным весом");
        }

        return shippingStrategyFactory.getAll().stream()
                .map(strategy ->
                        new ShipmentResponse(strategy.getCarrierName(), totalWeight, strategy.calculate(totalWeight)))
                .toList();
    }
}
