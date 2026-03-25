package kg.example.levantee.service.shipment.cdek.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CdekCity {

    @JsonProperty("code")
    private int code;

    @JsonProperty("city")
    private String city;

    @JsonProperty("region")
    private String region;

    @JsonProperty("country_code")
    private String countryCode;
}