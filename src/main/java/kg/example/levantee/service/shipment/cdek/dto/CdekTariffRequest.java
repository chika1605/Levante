package kg.example.levantee.service.shipment.cdek.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CdekTariffRequest {

    @JsonProperty("tariff_code")
    private int tariffCode;

    @JsonProperty("from_location")
    private Location fromLocation;

    @JsonProperty("to_location")
    private Location toLocation;

    private List<Package> packages;

    @Data
    @AllArgsConstructor
    public static class Location {
        private int code;
    }

    @Data
    @AllArgsConstructor
    public static class Package {
        private int weight; // в граммах
        private int length; // в см
        private int width;  // в см
        private int height; // в см
    }
}