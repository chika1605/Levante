package kg.example.levantee.dto.shipmentDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TariffInfo {
    private String id;          // "CDEK:136:270" — carrier:tariffCode:toCityCode
    private String carrierName;
    private int code;
    private String name;
    private String description;
    private int periodMin;
    private int periodMax;
    private double price;
    private String currency;
}