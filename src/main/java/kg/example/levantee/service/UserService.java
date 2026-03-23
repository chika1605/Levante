package kg.example.levantee.service;

import kg.example.levantee.dto.mapper.UserMapper;
import kg.example.levantee.dto.userDto.UserRequest;
import kg.example.levantee.dto.userDto.UserResponse;
import kg.example.levantee.model.entity.user.User;
import kg.example.levantee.repository.UserRepository;
import kg.example.levantee.utils.exception.AlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse register(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AlreadyExistsException("Пользователь с таким именем уже существует");
        }
        User user = userMapper.toEntity(request);
        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }
}
