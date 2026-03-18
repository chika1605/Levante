package kg.example.levantee.controller;

import jakarta.validation.Valid;
import kg.example.levantee.dto.orderDto.OrderRequest;
import kg.example.levantee.dto.orderDto.OrderResponse;
import kg.example.levantee.dto.orderDto.OrderSummaryResponse;
import kg.example.levantee.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> getAll() {
        return ResponseEntity.ok(orderService.getAll());
    }
}
