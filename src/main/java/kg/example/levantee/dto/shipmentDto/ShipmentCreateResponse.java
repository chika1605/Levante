package kg.example.levantee.dto.shipmentDto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private OrderStatus orderStatus;

    @JsonProperty("cdek_uuid")
    private String cdekUuid;

    @JsonProperty("cdek_request_status")
    private String cdekRequestStatus;   // ACCEPTED

    @JsonProperty("calculated_price")
    private double calculatedPrice;
}