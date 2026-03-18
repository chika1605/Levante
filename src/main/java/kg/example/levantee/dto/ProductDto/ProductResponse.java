package kg.example.levantee.dto.ProductDto;

import kg.example.levantee.model.enums.ProductStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
