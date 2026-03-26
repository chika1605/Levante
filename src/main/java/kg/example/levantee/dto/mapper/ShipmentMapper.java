package kg.example.levantee.dto.mapper;

import kg.example.levantee.dto.cdekDto.CdekCreateOrderRequest;
import kg.example.levantee.dto.cdekDto.CdekCreateOrderResponse;
import kg.example.levantee.dto.shipmentDto.ShipmentCreateResponse;
import kg.example.levantee.dto.shipmentDto.ShipmentPackage;
import kg.example.levantee.dto.shipmentDto.ShipmentRequest;
import kg.example.levantee.model.entity.order.Order;
import kg.example.levantee.model.entity.shipment.Shipment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShipmentMapper {

    public Shipment toEntity(Order order, ShipmentRequest request, String carrier, int tariffCode,
                             double deliveryPrice, double insurancePrice, double totalPrice,
                             double declaredValue, CdekCreateOrderResponse cdekResponse) {
        return Shipment.builder()
                .order(order)
                .carrier(carrier)
                .tariffId(request.getTariffId())
                .tariffCode(tariffCode)
                .tariffName(request.getTariffName())
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryPrice(deliveryPrice)
                .insurancePrice(insurancePrice)
                .calculatedPrice(totalPrice)
                .declaredValue(declaredValue)
                .cdekUuid(cdekResponse != null ? cdekResponse.getUuid() : null)
                .cdekRequestStatus(cdekResponse != null ? cdekResponse.getStatus() : null)
                .cdekPollAttempts(0)
                .build();
    }

    public ShipmentCreateResponse toCreateResponse(Shipment shipment, String cdekUuid, String cdekStatus, double totalPrice) {
        return new ShipmentCreateResponse(
                shipment.getId(),
                shipment.getOrder().getId(),
                shipment.getCarrier(),
                shipment.getTariffId(),
                shipment.getOrder().getStatus(),
                cdekUuid,
                cdekStatus,
                totalPrice
        );
    }

    public CdekCreateOrderRequest toCdekRequest(ShipmentRequest request, List<ShipmentPackage> packages,
                                                int tariffCode, int toCityCode,
                                                double insuranceAmount, int fromCityCode) {
        boolean isPvz = request.getDeliveryPoint() != null && !request.getDeliveryPoint().isBlank();

        CdekCreateOrderRequest cdekRequest = new CdekCreateOrderRequest();
        cdekRequest.setOrderId(request.getOrderId());
        cdekRequest.setTariffCode(tariffCode);
        cdekRequest.setFromCityCode(fromCityCode);
        cdekRequest.setToCityCode(toCityCode);
        cdekRequest.setRecipientName(request.getRecipientName());
        cdekRequest.setRecipientPhone(request.getRecipientPhone());
        cdekRequest.setRecipientEmail(request.getRecipientEmail());
        cdekRequest.setPackages(packages);
        cdekRequest.setInsuranceAmount(insuranceAmount > 0 ? insuranceAmount : null);

        if (isPvz) {
            cdekRequest.setDeliveryPoint(request.getDeliveryPoint());
        } else {
            cdekRequest.setToAddress(request.getDeliveryAddress());
        }

        return cdekRequest;
    }
}