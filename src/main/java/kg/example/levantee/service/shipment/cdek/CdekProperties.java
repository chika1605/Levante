package kg.example.levantee.service.shipment.cdek;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cdek.api")
public class CdekProperties {
    private String url;
    private String clientId;
    private String clientSecret;
    private int fromCityCode;
    private int toCityCode;
    private int tariffCode;
}
