package kg.example.levantee.service.shipment;

import kg.example.levantee.service.shipment.dto.CdekParams;
import kg.example.levantee.service.shipment.dto.ShipmentPackage;

import java.util.List;

public interface ShippingStrategy {
    double calculate(List<ShipmentPackage> packages, CdekParams cdekParams);
    String getCarrierName();
}