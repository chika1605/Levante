package kg.example.levantee.dto.cdekDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CdekCreateOrderResponse {
    private String uuid;        // внутренний UUID заказа в CDEK
    private String cdekNumber;  // номер для отслеживания (приходит позже, может быть null)
    private String status;      // ACCEPTED / ERROR
}