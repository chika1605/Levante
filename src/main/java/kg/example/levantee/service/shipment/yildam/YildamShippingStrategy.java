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
    public List<TariffInfo> getTariffs(int toCityCode, List<ShipmentPackage> packages) {
        List<TariffInfo> tariffs = yildamService.getTariffs(toCityCode);
        if (packages == null || packages.isEmpty()) return tariffs;

        ShipmentParams params = new ShipmentParams(0, toCityCode, 0);
        double realPrice = calculate(packages, params);
        return tariffs.stream()
                .map(t -> new TariffInfo(t.getId(), t.getCarrierName(), t.getCode(), t.getName(),
                        t.getDescription(), t.getPeriodMin(), t.getPeriodMax(), realPrice, t.getCurrency()))
                .toList();
    }
}