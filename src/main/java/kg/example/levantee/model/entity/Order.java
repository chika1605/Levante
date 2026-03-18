package kg.example.levantee.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import kg.example.levantee.model.enums.OrderStatus;
import kg.example.levantee.model.enums.OrderStatusConverter;
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
    private LocalDateTime orderDate;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private Integer totalQuantity;


    @Column(nullable = false)
    @Convert(converter = OrderStatusConverter.class)
    private OrderStatus status;

    @PrePersist
    public void prePersist() {
        this.status = OrderStatus.PENDING;
        this.orderDate = LocalDateTime.now();
    }
}
