package kg.example.levantee.model.entity.order;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import kg.example.levantee.model.entity.orderItem.OrderItem;
import kg.example.levantee.model.entity.user.User;
import kg.example.levantee.model.enums.order.OrderStatus;
import kg.example.levantee.model.enums.order.OrderStatusConverter;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<OrderItem> items;

    @Column(nullable = false)
    private LocalDateTime orderedDate;

    @Column(nullable = false)
    @Convert(converter = OrderStatusConverter.class)
    private OrderStatus status;

    private LocalDateTime updatedDate;

    @PrePersist
    public void prePersist() {
        this.status = OrderStatus.CART;
        this.orderedDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
