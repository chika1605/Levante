package kg.example.levantee.service.shipment.yildam;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "yildam.pricing")
public class YildamProperties {
    private double basePrice;
    private double maxBaseWeight;
    private double pricePerKg;
}