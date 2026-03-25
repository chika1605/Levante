package kg.example.levantee.dto.cdekDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CdekCalculateResponse {
    private double totalPrice;
    private String currency;
    private int periodMin;
    private int periodMax;
}