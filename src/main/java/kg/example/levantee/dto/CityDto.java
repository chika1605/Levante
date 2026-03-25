package kg.example.levantee.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CityDto {
    private int code;
    private String city;
    private String region;
    private String countryCode;
}