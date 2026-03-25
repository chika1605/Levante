package kg.example.levantee.dto.orderDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import kg.example.levantee.dto.orderItemDto.OrderItemResponse;
import kg.example.levantee.model.enums.order.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDetailResponse {
    private Long id;
    private String orderCode;
    private Long userId;
    private List<OrderItemResponse> items;
    private LocalDateTime orderedDate;
    private Double totalAmount;
    private Integer totalQuantity;
    private Double totalWeight;
    private OrderStatus status;

    // Доставка (заполняется если есть Shipment)
    @JsonProperty("cdek_status")
    private String cdekStatus;

    @JsonProperty("cdek_number")
    private String cdekNumber;

    @JsonProperty("calculated_price")
    private Double calculatedPrice;

    @JsonProperty("delivery_price")
    private Double deliveryPrice;

    @JsonProperty("insurance_price")
    private Double insurancePrice;

    @JsonProperty("declared_value")
    private Double declaredValue;
}
