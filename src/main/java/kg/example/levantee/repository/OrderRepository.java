package kg.example.levantee.repository;

import kg.example.levantee.dto.orderDto.OrderSummaryProjection;
import kg.example.levantee.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query(value = """
        SELECT o.id,
               o.order_code as orderCode,
               o.user_id as userId,
               o.ordered_date as orderedDate,
               o.total_amount as totalAmount,
               o.total_quantity as totalQuantity,
               o.status
        FROM orders o
        """, nativeQuery = true)
    Page<OrderSummaryProjection> findAllOrders(Pageable pageable);
}
