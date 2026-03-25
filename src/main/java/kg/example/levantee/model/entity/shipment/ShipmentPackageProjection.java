package kg.example.levantee.model.entity.shipment;

public interface ShipmentPackageProjection {
    Double getWeightKg();
    Double getLengthCm();
    Double getWidthCm();
    Double getHeightCm();
    Integer getQuantity();
}
