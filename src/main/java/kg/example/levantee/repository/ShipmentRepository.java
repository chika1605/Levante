package kg.example.levantee.repository;

import kg.example.levantee.model.entity.shipment.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
}