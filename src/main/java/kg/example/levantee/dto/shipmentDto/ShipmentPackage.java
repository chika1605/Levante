package kg.example.levantee.dto.shipmentDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShipmentPackage {
    private double weightKg;
    private double lengthCm;
    private double widthCm;
    private double heightCm;
    private int quantity;
}