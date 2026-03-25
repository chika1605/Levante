package kg.example.levantee.repository;

import kg.example.levantee.model.entity.order.OrderSummaryProjection;
import kg.example.levantee.model.entity.order.Order;
import kg.example.levantee.model.entity.shipment.ShipmentPackageProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query(value = """
        SELECT o.id,
               o.order_code as orderCode,
               o.user_id as userId,
               o.ordered_date as orderedDate,
               oi.totalAmount as totalAmount,
               oi.totalQuantity as totalQuantity,
               oi.totalWeight as totalWeight,
               o.status
        FROM orders o
        LEFT JOIN (
                    SELECT oi.order_id,
                           SUM(oi.total_price)        as totalAmount,
                           SUM(oi.quantity)           as totalQuantity,
                           SUM(oi.quantity * p.weight) as totalWeight
                    FROM order_items oi
                    JOIN products p ON p.id = oi.product_id
                    GROUP BY oi.order_id
                  ) oi ON oi.order_id = o.id
        """,
        countQuery = "SELECT COUNT(DISTINCT o.id) " +
                "FROM orders o JOIN order_items oi ON oi.order_id = o.id",
        nativeQuery = true)
    Page<OrderSummaryProjection> findAllOrders(Pageable pageable);

    @Query(value = """
        SELECT p.weight   AS weightKg,
               p.length   AS lengthCm,
               p.width    AS widthCm,
               p.height   AS heightCm,
               oi.quantity AS quantity
        FROM order_items oi
        JOIN products p ON p.id = oi.product_id
        WHERE oi.order_id = :orderId
        """, nativeQuery = true)
    List<ShipmentPackageProjection> findShipmentPackages(@Param("orderId") Long orderId);

    @Query("SELECT SUM(oi.totalPrice) FROM OrderItem oi WHERE oi.order.id = :orderId")
    Double findTotalAmountByOrderId(@Param("orderId") Long orderId);
}
