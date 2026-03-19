package kg.example.levantee.dto.orderDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import kg.example.levantee.dto.orderItemDto.OrderItemRequest;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    @NotNull(message = "ID пользователя обязателен")
    private Long userId;

    @Valid
    @NotEmpty(message = "Список товаров не может быть пустым")
    private List<OrderItemRequest> items;
}
