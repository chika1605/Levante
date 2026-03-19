package kg.example.levantee.repository;

import kg.example.levantee.dto.orderDto.OrderSummaryProjection;
import kg.example.levantee.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("""
        SELECT o.id as id,
               o.orderCode as orderCode,
               o.user.id as userId,
               o.orderedDate as orderedDate,
               o.totalAmount as totalAmount,
               o.totalQuantity as totalQuantity,
               o.status as status
        FROM Order o
        """)
    Page<OrderSummaryProjection> findAllOrders(Pageable pageable);
}
