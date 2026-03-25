package kg.example.levantee.service.shipment.cdek.model;

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
    private int tariffCode;
    private String senderName;
    private String senderPhone;
    private String senderAddress;
}
