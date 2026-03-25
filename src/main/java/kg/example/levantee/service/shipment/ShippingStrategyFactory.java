package kg.example.levantee.service.shipment;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ShippingStrategyFactory {

    private final Map<String, ShippingStrategy> strategies;

    public ShippingStrategyFactory(List<ShippingStrategy> strategyList) {
        strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        ShippingStrategy::getCarrierName,
                        s -> s
                ));
    }

    public List<ShippingStrategy> getAll() {
        return List.copyOf(strategies.values());
    }

    public ShippingStrategy get(String carrierCode) {
        if (carrierCode == null || carrierCode.isBlank()) {
            throw new IllegalArgumentException("Код службы доставки не может быть пустым");
        }
        ShippingStrategy strategy = strategies.get(carrierCode.trim().toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Неизвестная служба доставки: " + carrierCode);
        }
        return strategy;
    }
}