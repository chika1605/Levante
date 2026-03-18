package kg.example.levantee.controller;

import jakarta.validation.Valid;
import kg.example.levantee.dto.userDto.UserRequest;
import kg.example.levantee.dto.userDto.UserResponse;
import kg.example.levantee.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }
}
