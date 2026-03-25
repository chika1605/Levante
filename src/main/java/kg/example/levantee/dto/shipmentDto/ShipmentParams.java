package kg.example.levantee.dto.shipmentDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShipmentParams {
    private int fromCityCode;
    private int toCityCode;
    private int tariffCode;
}