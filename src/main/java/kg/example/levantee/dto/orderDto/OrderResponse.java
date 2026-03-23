package kg.example.levantee.dto.orderDto;

import kg.example.levantee.dto.orderItemDto.OrderItemResponse;
import kg.example.levantee.model.enums.order.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private String orderCode;
    private Long userId;
    private List<OrderItemResponse> items;
    private LocalDateTime orderedDate;
    private Double totalAmount;
    private Integer totalQuantity;
    private Double totalWeight;
    private OrderStatus status;
}
