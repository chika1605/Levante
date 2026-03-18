package kg.example.levantee.dto.OrderDto;

import io.swagger.v3.oas.annotations.media.Schema;
import kg.example.levantee.model.enums.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private String orderCode;
    private Long userId;
    private List<OrderItemResponse> items;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private Integer totalQuantity;
    @Schema(type = "integer", example = "1", description = "1=PENDING, 2=CONFIRMED, 3=CANCELLED")
    private OrderStatus status;
}
