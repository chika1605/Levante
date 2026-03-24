package kg.example.levantee.service.shipment.impl;

import kg.example.levantee.service.shipment.ShippingStrategy;
import kg.example.levantee.service.shipment.dto.CdekParams;
import kg.example.levantee.service.shipment.dto.ShipmentPackage;
import kg.example.levantee.service.shipment.yildam.YildamProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class YildamShippingStrategy implements ShippingStrategy {

    private final YildamProperties properties;

    @Override
    public double calculate(List<ShipmentPackage> packages, CdekParams cdekParams) {
        double totalWeight = packages.stream()
                .mapToDouble(p -> p.getWeightKg() * p.getQuantity())
                .sum();
        if (totalWeight <= properties.getMaxBaseWeight()) {
            return properties.getBasePrice();
        }
        return properties.getBasePrice()
                + (totalWeight - properties.getMaxBaseWeight()) * properties.getPricePerKg();
    }

    @Override
    public String getCarrierName() {
        return "YILDAM";
    }
}