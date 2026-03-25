package kg.example.levantee.service.shipment.cdek.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CdekTariffRequest {

    @JsonProperty("tariff_code")
    private Integer tariffCode;

    private String date;

    @JsonProperty("from_location")
    private Location fromLocation;

    @JsonProperty("to_location")
    private Location toLocation;

    private List<Package> packages;

    private List<Service> services;

    @Data
    @AllArgsConstructor
    public static class Location {
        private int code;
    }

    @Data
    @AllArgsConstructor
    public static class Service {
        private String code;
        private String parameter;
    }

    @Data
    @AllArgsConstructor
    public static class Package {
        private int weight;
        private int length;
        private int width;
        private int height;
    }
}