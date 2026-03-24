package kg.example.levantee.model.entity.product;

import java.time.LocalDateTime;

public interface ProductProjection {
    Long getId();
    String getCode();
    String getName();
    String getDescription();
    Double getPrice();
    Double getWeight();
    Double getLength();
    Double getWidth();
    Double getHeight();
    Integer getStock();
    Short  getStatus();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}