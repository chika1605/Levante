package kg.example.levantee.model.entity.shipment;

import jakarta.persistence.*;
import kg.example.levantee.model.entity.order.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false)
    private String carrier;

    @Column(nullable = false)
    private String tariffId;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String recipientPhone;

    private String deliveryAddress;

    private Integer tariffCode;
    private String tariffName;

    private Double deliveryPrice;
    private Double insurancePrice;
    private Double calculatedPrice;
    private Double declaredValue;

    private String cdekUuid;
    private String cdekNumber;
    private String cdekStatus;
    private String cdekRequestStatus;
    private int cdekPollAttempts;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}