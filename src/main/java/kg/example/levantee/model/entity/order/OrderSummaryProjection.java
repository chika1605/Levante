package kg.example.levantee.model.entity.order;

import java.time.LocalDateTime;

public interface OrderSummaryProjection {
    Long getId();
    String getOrderCode();
    Long getUserId();
    LocalDateTime getOrderedDate();
    Double getTotalAmount();
    Integer getTotalQuantity();
    Double getTotalWeight();
    Short getStatus();
}