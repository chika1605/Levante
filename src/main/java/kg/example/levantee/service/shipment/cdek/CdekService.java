package kg.example.levantee.service.shipment.cdek;

import kg.example.levantee.dto.CityDto;
import kg.example.levantee.dto.cdekDto.CdekCreateOrderRequest;
import kg.example.levantee.dto.cdekDto.CdekCreateOrderResponse;
import kg.example.levantee.dto.shipmentDto.ShipmentPackage;
import kg.example.levantee.dto.shipmentDto.TariffInfo;
import kg.example.levantee.service.shipment.cdek.client.CdekClient;
import kg.example.levantee.service.shipment.cdek.model.CdekProperties;
import kg.example.levantee.service.shipment.cdek.model.CdekOrderApiRequest;
import kg.example.levantee.service.shipment.cdek.model.CdekOrderApiResponse;
import kg.example.levantee.service.shipment.cdek.model.CdekTariffListResponse;
import kg.example.levantee.service.shipment.cdek.model.CdekTariffRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CdekService {

    private final CdekClient cdekClient;
    private final CdekProperties properties;


    public List<TariffInfo> getSupportedTariffs(int toCityCode, List<ShipmentPackage> packages) {
        CdekTariffListResponse response = cdekClient.getTariffList(toCityCode, packages);
        return response.getTariffCodes().stream()
                .map(t -> new TariffInfo(
                        "CDEK:" + t.getTariffCode() + ":" + toCityCode,
                        "CDEK",
                        t.getTariffCode(),
                        t.getTariffName(),
                        t.getTariffDescription(),
                        t.getPeriodMin() != null ? t.getPeriodMin() : (t.getCalendarMin() != null ? t.getCalendarMin() : 0),
                        t.getPeriodMax() != null ? t.getPeriodMax() : (t.getCalendarMax() != null ? t.getCalendarMax() : 0),
                        (t.getTotalSum() != null && t.getTotalSum() > 0) ? t.getTotalSum() : (t.getDeliverySum() != null ? t.getDeliverySum() : 0.0),
                        "RUB"))
                .toList();
    }

    public List<CityDto> searchCities(String name) {
        return cdekClient.searchCities(name).stream()
                .map(c -> new CityDto(c.getCode(), c.getCity(), c.getRegion(), c.getCountryCode()))
                .toList();
    }

    public CdekCreateOrderResponse createOrder(CdekCreateOrderRequest request) {
        boolean isWarehouse = request.getDeliveryPoint() != null && !request.getDeliveryPoint().isBlank();

        String deliveryPoint = isWarehouse ? request.getDeliveryPoint() : null;
        CdekOrderApiRequest.Location toLocation = isWarehouse
                ? null
                : CdekOrderApiRequest.Location.ofCityAndAddress(request.getToCityCode(), request.getToAddress());

        int fromCityCode = request.getFromCityCode() > 0 ? request.getFromCityCode() : properties.getFromCityCode();

        CdekOrderApiRequest.Sender sender = CdekOrderApiRequest.Sender.builder()
                .name(properties.getSenderName())
                .company(properties.getSenderName())
                .phones(List.of(CdekOrderApiRequest.Recipient.Phone.builder()
                        .number(properties.getSenderPhone()).build()))
                .build();

        CdekOrderApiRequest.Recipient recipient = CdekOrderApiRequest.Recipient.builder()
                .name(request.getRecipientName())
                .phones(List.of(CdekOrderApiRequest.Recipient.Phone.builder()
                        .number(request.getRecipientPhone()).build()))
                .email(request.getRecipientEmail())
                .build();

        List<CdekOrderApiRequest.Package> packages = new ArrayList<>();
        int num = 1;
        for (var item : request.getPackages()) {
            int wg = (int) Math.max(100, Math.round(item.getWeightKg() * 1000));
            for (int i = 0; i < item.getQuantity(); i++) {
                packages.add(CdekOrderApiRequest.Package.builder()
                        .number(String.valueOf(num++))
                        .comment("Место " + num)
                        .weight(wg)
                        .length((int) Math.max(1, item.getLengthCm()))
                        .width((int) Math.max(1, item.getWidthCm()))
                        .height((int) Math.max(1, item.getHeightCm()))
                        .items(List.of())
                        .build());
            }
        }

        List<CdekTariffRequest.Service> services = request.getInsuranceAmount() != null && request.getInsuranceAmount() > 0
                ? List.of(new CdekTariffRequest.Service("INSURANCE", String.valueOf(request.getInsuranceAmount().intValue())))
                : null;

        CdekOrderApiRequest orderRequest = CdekOrderApiRequest.builder()
                .type(2)
                .tariffCode(request.getTariffCode())
                .deliveryPoint(deliveryPoint)
                .fromLocation(CdekOrderApiRequest.Location.ofCityAndAddress(fromCityCode, properties.getSenderAddress()))
                .toLocation(toLocation)
                .sender(sender)
                .recipient(recipient)
                .packages(packages)
                .services(services)
                .build();

        CdekOrderApiResponse response = cdekClient.createOrder(orderRequest);

        String cdekStatus = (response.getRequests() != null && !response.getRequests().isEmpty())
                ? response.getRequests().get(0).getState()
                : "UNKNOWN";

        return new CdekCreateOrderResponse(response.getEntity().getUuid(),
                response.getEntity().getCdekNumber(), cdekStatus);
    }
}