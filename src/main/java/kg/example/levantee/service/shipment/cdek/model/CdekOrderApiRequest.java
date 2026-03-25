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

    @JsonProperty("tariff_code")
    private int tariffCode;

    @JsonProperty("from_location")
    private Location fromLocation;

    @JsonProperty("to_location")
    private Location toLocation;

    private Recipient recipient;

    private List<CdekTariffRequest.Package> packages;

    private List<CdekTariffRequest.Service> services;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Location {
        private Integer code;       // код города (для DOOR — город назначения)
        private String address;     // адрес (для DOOR)

        @JsonProperty("postal_code")
        private String postalCode;

        public static Location ofCity(int cityCode) {
            return Location.builder().code(cityCode).build();
        }

        public static Location ofDeliveryPoint(String deliveryPointCode) {
            // ПВЗ передаётся через поле code на уровне заказа
            return Location.builder().code(null).address(deliveryPointCode).build();
        }

        public static Location ofAddress(String address) {
            return Location.builder().address(address).build();
        }
    }

    @Data
    @Builder
    public static class Recipient {
        private String name;
        private List<Phone> phones;

        @Data
        @Builder
        public static class Phone {
            private String number;
        }
    }
}