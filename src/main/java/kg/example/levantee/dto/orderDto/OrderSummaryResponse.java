package kg.example.levantee.dto.orderDto;

import kg.example.levantee.model.enums.order.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderSummaryResponse {
    private Long id;
    private String orderCode;
    private Long userId;
    private LocalDateTime orderedDate;
    private Double totalAmount;
    private Integer totalQuantity;
    private Double totalWeight;
    private OrderStatus status;
}
