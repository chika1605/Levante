package kg.example.levantee.dto.productDto;

import java.time.LocalDateTime;

public interface ProductProjection {
    Long getId();
    String getCode();
    String getName();
    String getDescription();
    Double getPrice();
    Integer getStock();
    Short  getStatus();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}