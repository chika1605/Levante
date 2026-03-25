package kg.example.levantee.service.shipment.cdek.strategy;

import kg.example.levantee.dto.shipmentDto.ShipmentPackage;
import kg.example.levantee.dto.shipmentDto.ShipmentParams;
import kg.example.levantee.dto.shipmentDto.TariffInfo;
import kg.example.levantee.service.shipment.ShippingStrategy;
import kg.example.levantee.service.shipment.cdek.client.CdekClient;
import kg.example.levantee.service.shipment.cdek.service.CdekService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CdekShippingStrategy implements ShippingStrategy {

    private final CdekClient cdekClient;
    private final CdekService cdekService;

    @Override
    public String getCarrierName() {
        return "CDEK";
    }

    @Override
    public double calculate(List<ShipmentPackage> packages, ShipmentParams params) {
        return cdekClient.calculateTariff(packages, params);
    }

    @Override
    public List<TariffInfo> getTariffs(int toCityCode) {
        return cdekService.getSupportedTariffs(toCityCode);
    }
}