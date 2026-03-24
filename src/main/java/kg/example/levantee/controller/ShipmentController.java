package kg.example.levantee.controller;

import jakarta.validation.constraints.Positive;
import kg.example.levantee.dto.shipmentDto.ShipmentResponse;
import kg.example.levantee.service.shipment.ShipmentService;
import kg.example.levantee.service.shipment.dto.CdekParams;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/shipment")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping("/calculate")
    public ResponseEntity<List<ShipmentResponse>> calculate(
            @RequestParam @Positive(message = "ID заказа должен быть положительным") Long orderId,
            @RequestParam(defaultValue = "0") int fromCityCode,
            @RequestParam(defaultValue = "0") int toCityCode,
            @RequestParam(defaultValue = "0") int tariffCode) {
        CdekParams cdekParams = new CdekParams(fromCityCode, toCityCode, tariffCode);
        return ResponseEntity.ok(shipmentService.calculateAll(orderId, cdekParams));
    }
}