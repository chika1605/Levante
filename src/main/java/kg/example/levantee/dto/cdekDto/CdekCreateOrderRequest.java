package kg.example.levantee.dto.cdekDto;

import kg.example.levantee.dto.shipmentDto.ShipmentPackage;
import lombok.Data;

import java.util.List;

@Data
public class CdekCreateOrderRequest {
    private Long orderId;
    private int tariffCode;
    private int fromCityCode;
    private String deliveryPoint;
    private String toAddress;
    private int toCityCode;
    private String recipientName;
    private String recipientPhone;
    private String recipientEmail;
    private List<ShipmentPackage> packages;
    private Double insuranceAmount;
}