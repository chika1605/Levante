package kg.example.levantee.dto.cdekDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CdekCreateOrderResponse {
    private String uuid;
    private String cdekNumber;
    private String status;
}