package kg.example.levantee.dto.productDto;

import kg.example.levantee.model.enums.product.ProductStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Double price;
    private Double weight;
    private Double length;
    private Double width;
    private Double height;
    private Integer stock;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
