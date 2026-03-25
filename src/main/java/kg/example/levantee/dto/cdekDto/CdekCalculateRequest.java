package kg.example.levantee.dto.cdekDto;

import kg.example.levantee.dto.shipmentDto.ShipmentPackage;
import lombok.Data;

import java.util.List;

@Data
public class CdekCalculateRequest {
    private int fromCityCode;
    private int toCityCode;
    private int tariffCode;
    private List<ShipmentPackage> packages;
    private Double insuranceAmount;
}