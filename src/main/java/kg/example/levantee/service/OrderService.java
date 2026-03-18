package kg.example.levantee.service;

import jakarta.transaction.Transactional;
import kg.example.levantee.dto.orderDto.OrderItemRequest;
import kg.example.levantee.dto.orderDto.OrderRequest;
import kg.example.levantee.dto.orderDto.OrderResponse;
import kg.example.levantee.dto.mapper.OrderMapper;
import kg.example.levantee.model.entity.Order;
import kg.example.levantee.model.entity.OrderItem;
import kg.example.levantee.model.entity.Product;
import kg.example.levantee.model.entity.User;
import kg.example.levantee.repository.OrderRepository;
import kg.example.levantee.repository.ProductRepository;
import kg.example.levantee.repository.UserRepository;
import kg.example.levantee.utils.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse create(OrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Order order = orderMapper.toEntity(user, request, new ArrayList<>(), 0, 0);
        order = orderRepository.save(order);

        List<OrderItem> items = new ArrayList<>();
        double totalAmount = 0;
        int totalQuantity = 0;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new NotFoundException("Продукт не найден"));

            if (product.getStock() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Недостаточно товара на складе: " + product.getName());
            }

            product.setStock(product.getStock() - itemRequest.getQuantity());

            OrderItem item = orderMapper.toItem(order, product, itemRequest.getQuantity());
            items.add(item);
            totalAmount += item.getTotalPrice();
            totalQuantity += item.getQuantity();
        }

        order.setItems(items);
        order.setTotalAmount(totalAmount);
        order.setTotalQuantity(totalQuantity);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    public List<OrderResponse> getAll() {
        return orderRepository.findAll().stream().map(orderMapper::toResponse).toList();
    }
}
