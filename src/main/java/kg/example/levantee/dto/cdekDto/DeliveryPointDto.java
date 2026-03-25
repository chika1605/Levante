package kg.example.levantee.dto.cdekDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeliveryPointDto {
    private String code;
    private String name;
    private String type;
    private String address;
    private String workTime;
}