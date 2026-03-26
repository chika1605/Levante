package kg.example.levantee.service.shipment.cdek.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CdekOrderApiRequest {

    private int type;

    @JsonProperty("tariff_code")
    private int tariffCode;

    @JsonProperty("delivery_point")
    private String deliveryPoint;

    @JsonProperty("from_location")
    private Location fromLocation;

    @JsonProperty("to_location")
    private Location toLocation;

    private Sender sender;

    private Recipient recipient;

    private List<Package> packages;

    private List<CdekTariffRequest.Service> services;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Location {
        private Integer code;
        private String city;
        private String address;

        @JsonProperty("postal_code")
        private String postalCode;

        public static Location ofCityAndAddress(int cityCode, String address) {
            return Location.builder().code(cityCode).address(address).build();
        }

        public static Location ofDeliveryPoint(String deliveryPointCode) {
            return Location.builder().address(deliveryPointCode).build();
        }
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Sender {
        private String name;
        private String company;
        private List<Recipient.Phone> phones;
        private String email;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Recipient {
        private String name;
        private List<Phone> phones;
        private String email;

        @Data
        @Builder
        public static class Phone {
            private String number;
        }
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Package {
        private String number;
        private String comment;
        private int weight;
        private int length;
        private int width;
        private int height;
        private List<Object> items;
    }
}