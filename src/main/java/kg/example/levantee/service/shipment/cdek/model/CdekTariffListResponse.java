package kg.example.levantee.service.shipment.cdek.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CdekTariffListResponse {

    @JsonProperty("tariff_codes")
    private List<TariffItem> tariffCodes;

    @Data
    public static class TariffItem {

        @JsonProperty("tariff_code")
        private int tariffCode;

        @JsonProperty("tariff_name")
        private String tariffName;

        @JsonProperty("tariff_description")
        private String tariffDescription;

        @JsonProperty("delivery_mode")
        private int deliveryMode;

        @JsonProperty("period_min")
        private Integer periodMin;

        @JsonProperty("period_max")
        private Integer periodMax;

        @JsonProperty("calendar_min")
        private Integer calendarMin;

        @JsonProperty("calendar_max")
        private Integer calendarMax;

        @JsonProperty("delivery_sum")
        private Double deliverySum;

        @JsonProperty("total_sum")
        private Double totalSum;
    }
}
