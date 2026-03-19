package kg.example.levantee.repository;

import jakarta.persistence.LockModeType;
import kg.example.levantee.dto.productDto.ProductProjection;
import kg.example.levantee.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findAllByIdWithLock(@Param("ids") List<Long> ids);
    boolean existsByCode(String code);
    @Query("""
        SELECT p.id as id,
               p.code as code,
               p.name as name,
               p.description as description,
               p.price as price,
               p.stock as stock,
               p.status as status,
               p.createdAt as createdAt,
               p.updatedAt as updatedAt
        FROM Product p
        """)
    Page<ProductProjection> findAllProducts(Pageable pageable);
}
