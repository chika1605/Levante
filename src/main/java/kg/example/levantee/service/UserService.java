package kg.example.levantee.service;

import kg.example.levantee.dto.userDto.UserRequest;
import kg.example.levantee.dto.userDto.UserResponse;
import kg.example.levantee.model.entity.User;
import kg.example.levantee.repository.UserRepository;
import kg.example.levantee.utils.exception.AlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse register(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AlreadyExistsException("Пользователь с таким именем уже существует");
        }

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
