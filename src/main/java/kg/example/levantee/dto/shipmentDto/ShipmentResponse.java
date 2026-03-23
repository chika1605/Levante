package kg.example.levantee.dto.shipmentDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShipmentResponse {
    private String carrierName;
    private double totalWeight;
    private double price;
}