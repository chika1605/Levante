package kg.example.levantee.repository;

import kg.example.levantee.model.entity.product.ProductProjection;
import kg.example.levantee.model.entity.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query(value = """
        SELECT *
        FROM products
        WHERE id IN :ids
        FOR UPDATE
        """, nativeQuery = true)
    List<Product> findAllByIdWithLock(@Param("ids") List<Long> ids);
    boolean existsByCode(String code);
    @Query(value = """
        SELECT p.id,
               p.code,
               p.name,
               p.description,
               p.price,
               p.weight,
               p.length,
               p.width,
               p.height,
               p.stock,
               p.status,
               p.created_at,
               p.updated_at
        FROM products p
        """, nativeQuery = true)
    Page<ProductProjection> findAllProducts(Pageable pageable);
}
