package kg.example.levantee.model.entity.product;

import jakarta.persistence.*;
import kg.example.levantee.model.enums.product.ProductStatus;
import kg.example.levantee.model.enums.product.ProductStatusConverter;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Double length;

    @Column(nullable = false)
    private Double width;

    @Column(nullable = false)
    private Double height;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    @Convert(converter = ProductStatusConverter.class)
    private ProductStatus status;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.status = ProductStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
