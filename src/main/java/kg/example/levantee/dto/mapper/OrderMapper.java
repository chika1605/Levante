package kg.example.levantee.dto.mapper;

import kg.example.levantee.model.entity.order.OrderSummaryProjection;
import kg.example.levantee.dto.orderItemDto.OrderItemResponse;
import kg.example.levantee.dto.orderDto.OrderRequest;
import kg.example.levantee.dto.orderDto.OrderResponse;
import kg.example.levantee.dto.orderDto.OrderSummaryResponse;
import kg.example.levantee.model.entity.order.Order;
import kg.example.levantee.model.entity.orderItem.OrderItem;
import kg.example.levantee.model.entity.product.Product;
import kg.example.levantee.model.entity.user.User;
import kg.example.levantee.model.enums.order.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class OrderMapper {

    public Order toEntity(User user, OrderRequest request) {
        String orderCode = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return Order.builder()
                .orderCode(orderCode)
                .user(user)
                .items(new ArrayList<>())
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
        response.setWeight(item.getProduct().getWeight());
        response.setLength(item.getProduct().getLength());
        response.setWidth(item.getProduct().getWidth());
        response.setHeight(item.getProduct().getHeight());
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
        response.setStatus(order.getStatus());
        List<OrderItem> items = order.getItems();
        response.setItems(items.stream().map(this::toItemResponse).toList());
        response.setTotalAmount(items.stream().mapToDouble(OrderItem::getTotalPrice).sum());
        response.setTotalQuantity(items.stream().mapToInt(OrderItem::getQuantity).sum());
        response.setTotalWeight(items.stream()
                .mapToDouble(item -> item.getProduct().getWeight() != null
                        ? item.getProduct().getWeight() * item.getQuantity() : 0)
                .sum());
        return response;
    }

    public OrderSummaryResponse toSummaryResponse(OrderSummaryProjection p) {
        OrderSummaryResponse response = new OrderSummaryResponse();
        response.setId(p.getId());
        response.setOrderCode(p.getOrderCode());
        response.setUserId(p.getUserId());
        response.setOrderedDate(p.getOrderedDate());
        response.setTotalAmount(p.getTotalAmount());
        response.setTotalQuantity(p.getTotalQuantity());
        response.setTotalWeight(p.getTotalWeight());
        response.setStatus(OrderStatus.fromId(p.getStatus()));
        return response;
    }
}