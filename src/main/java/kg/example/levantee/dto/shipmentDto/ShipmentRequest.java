package kg.example.levantee.dto.shipmentDto;

import lombok.Data;

@Data
public class ShipmentRequest {
    private Long orderId;
    private String tariffId;
    private String tariffName;
    private String recipientName;
    private String recipientPhone;
    private String recipientEmail;
    private String deliveryAddress;
}