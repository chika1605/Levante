package kg.example.levantee.dto.productDto;

import kg.example.levantee.model.enums.product.ProductStatus;

import java.time.LocalDateTime;

public interface ProductProjection {
    Long getId();
    String getCode();
    String getName();
    String getDescription();
    Double getPrice();
    Integer getStock();
    ProductStatus getStatus();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}