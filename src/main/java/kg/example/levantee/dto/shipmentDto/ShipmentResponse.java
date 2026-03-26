package kg.example.levantee.dto.shipmentDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShipmentResponse {

    @JsonProperty("delivery_price")
    private double deliveryPrice;

    @JsonProperty("insurance_price")
    private double insurancePrice;

    @JsonProperty("total_price")
    private double totalPrice;

    @JsonProperty("declared_value")
    private double declaredValue;

    @JsonProperty("insurance_enabled")
    private boolean insuranceEnabled;

    @JsonProperty("grand_total")
    private double grandTotal;
}