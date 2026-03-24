package kg.example.levantee.service.shipment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CdekParams {
    private int fromCityCode;
    private int toCityCode;
    private int tariffCode;
}