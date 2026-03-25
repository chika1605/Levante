package kg.example.levantee.service.shipment.yildam;

import kg.example.levantee.dto.shipmentDto.ShipmentPackage;
import kg.example.levantee.dto.shipmentDto.ShipmentParams;
import kg.example.levantee.dto.shipmentDto.TariffInfo;
import kg.example.levantee.service.shipment.ShippingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class YildamShippingStrategy implements ShippingStrategy {

    private final YildamProperties properties;
    private final YildamService yildamService;

    @Override
    public String getCarrierName() {
        return "YILDAM";
    }

    @Override
    public double calculate(List<ShipmentPackage> packages, ShipmentParams params) {
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
    public List<TariffInfo> getTariffs(int toCityCode) {
        return yildamService.getTariffs(toCityCode);
    }
}