package kg.example.levantee.controller;

import kg.example.levantee.dto.UserDto.UserRequest;
import kg.example.levantee.dto.UserDto.UserResponse;
import kg.example.levantee.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }
}
