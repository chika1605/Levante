package kg.example.levantee.repository;

import kg.example.levantee.model.entity.shipment.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByOrderId(Long orderId);
    Optional<Shipment> findByCdekUuid(String cdekUuid);
    List<Shipment> findAllByCdekRequestStatus(String cdekRequestStatus);
}