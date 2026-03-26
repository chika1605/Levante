package kg.example.levantee.service.shipment;

import kg.example.levantee.dto.shipmentDto.ShipmentPackage;
import kg.example.levantee.dto.shipmentDto.ShipmentParams;
import kg.example.levantee.dto.shipmentDto.TariffInfo;

import java.util.List;

public interface ShippingStrategy {
    String getCarrierName();
    double calculate(List<ShipmentPackage> packages, ShipmentParams params);
    List<TariffInfo> getTariffs(int toCityCode, List<ShipmentPackage> packages);
}