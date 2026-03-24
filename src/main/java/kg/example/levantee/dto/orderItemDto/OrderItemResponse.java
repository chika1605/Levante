package kg.example.levantee.dto.orderItemDto;

import lombok.Data;

@Data
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Double unitPrice;
    private Double weight;
    private Double length;
    private Double width;
    private Double height;
    private Integer quantity;
    private Double totalPrice;
}
