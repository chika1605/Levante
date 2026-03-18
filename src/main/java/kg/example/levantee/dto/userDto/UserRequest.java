package kg.example.levantee.dto.userDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "Логин обязателен")
    @Size(min = 3, max = 50, message = "Логин должен содержать от 3 до 50 символов")
    private String username;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, max = 100, message = "Пароль должен содержать от 6 до 100 символов")
    private String password;
}
