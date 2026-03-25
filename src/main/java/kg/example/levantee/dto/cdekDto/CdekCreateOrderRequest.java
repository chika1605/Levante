package kg.example.levantee.dto.cdekDto;

import kg.example.levantee.dto.shipmentDto.ShipmentPackage;
import lombok.Data;

import java.util.List;

@Data
public class CdekCreateOrderRequest {
    private Long orderId;   // ID заказа в нашей системе
    private int tariffCode;
    private int fromCityCode;

    // WAREHOUSE — пользователь выбрал ПВЗ
    private String deliveryPoint;  // код ПВЗ, например "MSK123"

    // DOOR — пользователь ввёл адрес
    private String toAddress;      // "ул. Ленина, 5"
    private int toCityCode;        // нужен для DOOR чтобы указать город

    private String recipientName;
    private String recipientPhone;
    private String recipientEmail;

    private List<ShipmentPackage> packages;

    private Double insuranceAmount; // null = без страховки
}