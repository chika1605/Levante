package kg.example.levantee.dto.mapper;


import kg.example.levantee.dto.orderDto.OrderItemResponse;
import kg.example.levantee.dto.orderDto.OrderRequest;
import kg.example.levantee.dto.orderDto.OrderResponse;
import kg.example.levantee.dto.orderDto.OrderSummaryResponse;
import kg.example.levantee.model.entity.Order;
import kg.example.levantee.model.entity.OrderItem;
import kg.example.levantee.model.entity.Product;
import kg.example.levantee.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class OrderMapper {

    public Order toEntity(User user, OrderRequest request, List<OrderItem> items, double totalAmount, int totalQuantity) {
        String orderCode = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return Order.builder()
                .orderCode(orderCode)
                .user(user)
                .items(items)
                .totalAmount(totalAmount)
                .totalQuantity(totalQuantity)
                .build();
    }

    public OrderItem toItem(Order order, Product product, int quantity) {
        double totalPrice = product.getPrice() * quantity;
        return OrderItem.builder()
                .order(order)
                .product(product)
                .unitPrice(product.getPrice())
                .quantity(quantity)
                .totalPrice(totalPrice)
                .build();
    }

    public OrderItemResponse toItemResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setUnitPrice(item.getUnitPrice());
        response.setQuantity(item.getQuantity());
        response.setTotalPrice(item.getTotalPrice());
        return response;
    }

    public OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderCode(order.getOrderCode());
        response.setUserId(order.getUser().getId());
        response.setOrderedDate(order.getOrderedDate());
        response.setTotalAmount(order.getTotalAmount());
        response.setTotalQuantity(order.getTotalQuantity());
        response.setStatus(order.getStatus());
        response.setItems(order.getItems().stream().map(this::toItemResponse).toList());
        return response;
    }

    public OrderSummaryResponse toSummaryResponse(Order order) {
        OrderSummaryResponse response = new OrderSummaryResponse();
        response.setId(order.getId());
        response.setOrderCode(order.getOrderCode());
        response.setUserId(order.getUser().getId());
        response.setOrderedDate(order.getOrderedDate());
        response.setTotalAmount(order.getTotalAmount());
        response.setTotalQuantity(order.getTotalQuantity());
        response.setStatus(order.getStatus());
        return response;
    }
}
