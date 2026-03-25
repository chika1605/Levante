package kg.example.levantee.service.shipment.cdek.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CdekTariffResponse {

    @JsonProperty("total_sum")
    private double totalSum;

    private String currency;

    @JsonProperty("delivery_sum")
    private double deliverySum;

    @JsonProperty("period_min")
    private int periodMin;

    @JsonProperty("period_max")
    private int periodMax;
}