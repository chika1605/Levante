package kg.example.levantee.dto.UserDto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private LocalDateTime createdDate;
}
