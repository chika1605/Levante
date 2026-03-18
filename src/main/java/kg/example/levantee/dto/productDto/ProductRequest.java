package kg.example.levantee.dto.productDto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductRequest {

    @NotBlank(message = "Код продукта обязателен")
    @Size(min = 2, max = 50, message = "Код продукта должен содержать от 2 до 50 символов")
    private String code;

    @NotBlank(message = "Название обязательно")
    @Size(min = 2, max = 150, message = "Название должно содержать от 2 до 150 символов")
    private String name;

    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;

    @NotNull(message = "Цена обязательна")
    @Positive(message = "Цена должна быть положительной")
    private Double price;

    @NotNull(message = "Количество на складе обязательно")
    @Min(value = 0, message = "Количество на складе не может быть отрицательным")
    private Integer stock;
}
