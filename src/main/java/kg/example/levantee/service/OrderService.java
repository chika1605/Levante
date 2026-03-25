package kg.example.levantee.service;

import jakarta.transaction.Transactional;
import kg.example.levantee.dto.mapper.OrderMapper;
import kg.example.levantee.dto.orderItemDto.OrderItemRequest;
import kg.example.levantee.dto.orderDto.OrderDetailResponse;
import kg.example.levantee.dto.orderDto.OrderRequest;
import kg.example.levantee.dto.orderDto.OrderResponse;
import kg.example.levantee.dto.orderDto.OrderSummaryResponse;
import kg.example.levantee.model.entity.order.Order;
import kg.example.levantee.model.entity.orderItem.OrderItem;
import kg.example.levantee.model.entity.product.Product;
import kg.example.levantee.model.entity.shipment.Shipment;
import kg.example.levantee.model.entity.user.User;
import kg.example.levantee.repository.OrderRepository;
import kg.example.levantee.repository.ProductRepository;
import kg.example.levantee.repository.ShipmentRepository;
import kg.example.levantee.repository.UserRepository;
import kg.example.levantee.utils.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ShipmentRepository shipmentRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse create(OrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Long> productIds = request.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .toList();

        Map<Long, Product> productMap = productRepository.findAllByIdWithLock(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        Order order = orderMapper.toEntity(user, request);
        order = orderRepository.save(order);

        List<OrderItem> items = new ArrayList<>();


        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productMap.get(itemRequest.getProductId());
            if (product == null) {
                throw new NotFoundException("Продукт не найден");
            }

            if (product.getStock() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Недостаточно товара на складе: " + product.getName());
            }

            product.setStock(product.getStock() - itemRequest.getQuantity());

            OrderItem item = orderMapper.toItem(order, product, itemRequest.getQuantity());
            items.add(item);
        }

        order.setItems(items);
        orderRepository.save(order);
        return orderMapper.toResponse(order);
    }

    public Page<OrderSummaryResponse> getAll(Pageable pageable) {
        return orderRepository.findAllOrders(pageable)
                .map(orderMapper::toSummaryResponse);
    }

    @Transactional
    public OrderDetailResponse getById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        OrderResponse base = orderMapper.toResponse(order);
        OrderDetailResponse detail = new OrderDetailResponse();
        detail.setId(base.getId());
        detail.setOrderCode(base.getOrderCode());
        detail.setUserId(base.getUserId());
        detail.setItems(base.getItems());
        detail.setOrderedDate(base.getOrderedDate());
        detail.setTotalAmount(base.getTotalAmount());
        detail.setTotalQuantity(base.getTotalQuantity());
        detail.setTotalWeight(base.getTotalWeight());
        detail.setStatus(base.getStatus());

        shipmentRepository.findByOrderId(id).ifPresent(s -> {
            detail.setCdekStatus(s.getCdekStatus());
            detail.setCdekNumber(s.getCdekNumber());
            detail.setCalculatedPrice(s.getCalculatedPrice());
            detail.setDeliveryPrice(s.getDeliveryPrice());
            detail.setInsurancePrice(s.getInsurancePrice());
            detail.setDeclaredValue(s.getDeclaredValue());
        });

        return detail;
    }
}