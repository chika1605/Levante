package kg.example.levantee.service;

import kg.example.levantee.dto.UserDto.UserRequest;
import kg.example.levantee.dto.UserDto.UserResponse;
import kg.example.levantee.model.entity.User;
import kg.example.levantee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl {

    private final UserRepository userRepository;

    public UserResponse register(UserRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .build();
        user = userRepository.save(user);

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setCreatedDate(user.getCreatedDate());
        return response;
    }
}
