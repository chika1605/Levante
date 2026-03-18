package kg.example.levantee.controller;

import jakarta.validation.Valid;
import kg.example.levantee.dto.OrderDto.OrderRequest;
import kg.example.levantee.dto.OrderDto.OrderResponse;
import kg.example.levantee.service.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderServiceImpl orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAll() {
        return ResponseEntity.ok(orderService.getAll());
    }
}
