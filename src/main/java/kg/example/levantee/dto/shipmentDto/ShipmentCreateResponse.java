package kg.example.levantee.dto.shipmentDto;

import kg.example.levantee.model.enums.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShipmentCreateResponse {
    private Long shipmentId;
    private Long orderId;
    private String carrier;
    private String tariffId;
    private OrderStatus orderStatus;  // всегда PENDING после создания
}