package kg.example.levantee.dto.mapper;

import kg.example.levantee.dto.userDto.UserRequest;
import kg.example.levantee.dto.userDto.UserResponse;
import kg.example.levantee.model.entity.user.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(UserRequest request) {
        return User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .build();
    }

    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setCreatedDate(user.getCreatedDate());
        return response;
    }
}
