package kg.example.levantee.service.shipment.cdek.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CdekDeliveryPoint {

    private String code;
    private String name;
    private String type;

    @JsonProperty("work_time")
    private String workTime;

    private Location location;

    @Data
    public static class Location {
        private String city;
        private String address;
    }
}