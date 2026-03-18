package kg.example.levantee.dto.ProductDto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductRequest {

    @NotBlank(message = "Код продукта обязателен")
    private String code;

    @NotBlank(message = "Название обязательно")
    private String name;

    private String description;

    @NotNull(message = "Цена обязательна")
    @Positive(message = "Цена должна быть положительной")
    private Double price;

    @NotNull(message = "Количество на складе обязательно")
    @Min(value = 0, message = "Количество на складе не может быть отрицательным")
    private Integer stock;
}
