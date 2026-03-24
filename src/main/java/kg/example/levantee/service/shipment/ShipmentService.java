package kg.example.levantee.service.shipment;

import kg.example.levantee.dto.shipmentDto.ShipmentResponse;
import kg.example.levantee.repository.OrderRepository;
import kg.example.levantee.service.shipment.dto.CdekParams;
import kg.example.levantee.service.shipment.dto.ShipmentPackage;
import kg.example.levantee.utils.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final OrderRepository orderRepository;
    private final ShippingStrategyFactory shippingStrategyFactory;

    public List<ShipmentResponse> calculateAll(Long orderId, CdekParams cdekParams) {
        if (!orderRepository.existsById(orderId)) {
            throw new NotFoundException("Заказ не найден");
        }

        List<ShipmentPackage> packages = orderRepository.findShipmentPackages(orderId);
        if (packages.isEmpty()) {
            throw new IllegalStateException("Заказ не содержит товаров");
        }

        double totalWeight = packages.stream()
                .mapToDouble(p -> p.getWeightKg() * p.getQuantity())
                .sum();

        return shippingStrategyFactory.getAll().stream()
                .map(strategy ->
                        new ShipmentResponse(strategy.getCarrierName(), totalWeight, strategy.calculate(packages, cdekParams)))
                .toList();
    }
}
