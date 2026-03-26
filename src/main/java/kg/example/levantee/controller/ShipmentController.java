package kg.example.levantee.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import kg.example.levantee.dto.shipmentDto.ShipmentCreateResponse;
import kg.example.levantee.dto.shipmentDto.ShipmentRequest;
import kg.example.levantee.dto.shipmentDto.ShipmentResponse;
import kg.example.levantee.dto.shipmentDto.TariffInfo;
import kg.example.levantee.service.shipment.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping
    public ResponseEntity<ShipmentCreateResponse> createShipment(@Valid @RequestBody ShipmentRequest request) {
        return ResponseEntity.ok(shipmentService.createShipment(request));
    }

    @GetMapping("/tariffs")
    public ResponseEntity<List<TariffInfo>> getTariffs(
            @RequestParam @Positive(message = "Укажите код города получения") int toCityCode,
            @RequestParam(required = false) Long orderId) {
        return ResponseEntity.ok(shipmentService.getTariffs(toCityCode, orderId));
    }

    @GetMapping("/calculate")
    public ResponseEntity<ShipmentResponse> calculate(
            @RequestParam @Positive(message = "ID заказа должен быть положительным") Long orderId,
            @RequestParam String tariffId,
            @RequestParam(defaultValue = "false") boolean insuranceEnabled) {
        return ResponseEntity.ok(shipmentService.calculate(orderId, tariffId, insuranceEnabled));
    }
}