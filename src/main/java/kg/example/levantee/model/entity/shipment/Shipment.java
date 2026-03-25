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
    private String carrier;         // CDEK / YILDAM

    @Column(nullable = false)
    private String tariffId;        // "CDEK:136:270"

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String recipientPhone;

    private String deliveryAddress; // адрес (DOOR) или код ПВЗ (WAREHOUSE)

    private Integer tariffCode;
    private String tariffName;

    private Double deliveryPrice;
    private Double insurancePrice;
    private Double calculatedPrice;  // фиксируется навсегда на момент оформления
    private Double declaredValue;

    // CDEK async tracking
    private String cdekUuid;            // entity.uuid из ответа CDEK
    private String cdekNumber;          // номер для клиента (приходит после SUCCESSFUL)
    private String cdekStatus;          // статус из CDEK (Создан / В пути / Вручен)
    private String cdekRequestStatus;   // ACCEPTED → SUCCESSFUL / INVALID
    private int cdekPollAttempts;       // счётчик попыток опроса статуса

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}