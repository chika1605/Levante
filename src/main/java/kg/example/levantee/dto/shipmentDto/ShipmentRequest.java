package kg.example.levantee.dto.shipmentDto;

import lombok.Data;

@Data
public class ShipmentRequest {
    private Long orderId;
    private String tariffId;         // "CDEK:136:270"
    private String tariffName;       // название тарифа (опционально)
    private String recipientName;
    private String recipientPhone;
    private String recipientEmail;
    private String deliveryAddress;  // адрес (DOOR) или код ПВЗ (WAREHOUSE)
}