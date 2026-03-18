package kg.example.levantee.dto.orderDto;

import lombok.Data;

@Data
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Double unitPrice;
    private Integer quantity;
    private Double price;
}
