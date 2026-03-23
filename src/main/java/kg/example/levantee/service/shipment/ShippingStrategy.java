package kg.example.levantee.service.shipment;

public interface ShippingStrategy {
    double calculate(double totalWeight);
    String getCarrierName();
}