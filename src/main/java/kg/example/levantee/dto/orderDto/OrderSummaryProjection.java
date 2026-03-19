package kg.example.levantee.dto.orderDto;

import kg.example.levantee.model.enums.order.OrderStatus;

import java.time.LocalDateTime;

public interface OrderSummaryProjection {
    Long getId();
    String getOrderCode();
    Long getUserId();
    LocalDateTime getOrderedDate();
    Double getTotalAmount();
    Integer getTotalQuantity();
    OrderStatus getStatus();
}