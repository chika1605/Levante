package kg.example.levantee.service.shipment.impl;

import kg.example.levantee.service.shipment.ShippingStrategy;
import kg.example.levantee.service.shipment.cdek.CdekClient;
import kg.example.levantee.service.shipment.dto.CdekParams;
import kg.example.levantee.service.shipment.dto.ShipmentPackage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CdekShippingStrategy implements ShippingStrategy {

    private final CdekClient cdekClient;

    @Override
    public double calculate(List<ShipmentPackage> packages, CdekParams cdekParams) {
        return cdekClient.calculateTariff(packages, cdekParams);
    }

    @Override
    public String getCarrierName() {
        return "CDEK";
    }
}