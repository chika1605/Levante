package kg.example.levantee.service.shipment.impl;

import kg.example.levantee.service.shipment.ShippingStrategy;
import org.springframework.stereotype.Component;

@Component
public class CdekShippingStrategy implements ShippingStrategy {

    private static final double BASE_PRICE = 200.0;
    private static final double MAX_BASE_WEIGHT = 4.0;
    private static final double PRICE_PER_KG = 45.0;

    @Override
    public double calculate(double totalWeight) {
        if (totalWeight <= MAX_BASE_WEIGHT) {
            return BASE_PRICE;
        }
        return BASE_PRICE + (totalWeight - MAX_BASE_WEIGHT) * PRICE_PER_KG;
    }

    @Override
    public String getCarrierName() {
        return "CDEK";
    }
}