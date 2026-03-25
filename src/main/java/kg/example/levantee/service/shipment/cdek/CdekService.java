package kg.example.levantee.service.shipment.cdek;

import kg.example.levantee.dto.CityDto;
import kg.example.levantee.dto.cdekDto.CdekCalculateRequest;
import kg.example.levantee.dto.cdekDto.CdekCalculateResponse;
import kg.example.levantee.dto.cdekDto.CdekCreateOrderRequest;
import kg.example.levantee.dto.cdekDto.CdekCreateOrderResponse;
import kg.example.levantee.dto.cdekDto.DeliveryPointDto;
import kg.example.levantee.dto.shipmentDto.ShipmentParams;
import kg.example.levantee.dto.shipmentDto.TariffInfo;
import kg.example.levantee.service.shipment.cdek.model.CdekOrderApiRequest;
import kg.example.levantee.service.shipment.cdek.model.CdekOrderApiResponse;
import kg.example.levantee.service.shipment.cdek.model.CdekTariffListResponse;
import kg.example.levantee.service.shipment.cdek.model.CdekTariffResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CdekService {

    private final CdekClient cdekClient;

    // Шаг 3: Список всех тарифов с ценами (fromCity всегда из properties)
    public List<TariffInfo> getSupportedTariffs(int toCityCode) {
        CdekTariffListResponse response = cdekClient.getTariffList(toCityCode);

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
                        "RUB"
                ))
                .toList();
    }

    // Поиск городов по названию (чтобы узнать cityCode)
    public List<CityDto> searchCities(String name) {
        return cdekClient.searchCities(name).stream()
                .map(c -> new CityDto(c.getCode(), c.getCity(), c.getRegion(), c.getCountryCode()))
                .toList();
    }

    // Шаг 2: Список ПВЗ (только если пользователь выбрал WAREHOUSE)
    public List<DeliveryPointDto> getDeliveryPoints(int cityCode) {
        return cdekClient.getDeliveryPoints(cityCode).stream()
                .map(p -> new DeliveryPointDto(
                        p.getCode(),
                        p.getName(),
                        p.getType(),
                        p.getLocation() != null ? p.getLocation().getAddress() : null,
                        p.getWorkTime()
                ))
                .toList();
    }

    // Шаг 4: Расчёт конкретного тарифа (с опциональной страховкой)
    public CdekCalculateResponse calculateSingleTariff(CdekCalculateRequest request) {
        ShipmentParams params = new ShipmentParams(
                request.getFromCityCode(),
                request.getToCityCode(),
                request.getTariffCode()
        );

        CdekTariffResponse response = cdekClient.calculateSingleTariff(
                request.getPackages(),
                params,
                request.getInsuranceAmount()
        );

        return new CdekCalculateResponse(
                response.getTotalSum(),
                response.getCurrency(),
                response.getPeriodMin(),
                response.getPeriodMax()
        );
    }

    // Шаг 5: Создание заказа
    public CdekCreateOrderResponse createOrder(CdekCreateOrderRequest request) {
        boolean isWarehouse = request.getDeliveryPoint() != null && !request.getDeliveryPoint().isBlank();

        CdekOrderApiRequest.Location toLocation = isWarehouse
                ? CdekOrderApiRequest.Location.ofDeliveryPoint(request.getDeliveryPoint())
                : CdekOrderApiRequest.Location.ofAddress(request.getToAddress());

        List<CdekOrderApiRequest.Recipient.Phone> phones = List.of(
                CdekOrderApiRequest.Recipient.Phone.builder()
                        .number(request.getRecipientPhone())
                        .build()
        );

        CdekOrderApiRequest.Recipient recipient = CdekOrderApiRequest.Recipient.builder()
                .name(request.getRecipientName())
                .phones(phones)
                .build();

        List<kg.example.levantee.service.shipment.cdek.model.CdekTariffRequest.Package> packages =
                request.getPackages().stream()
                        .flatMap(item -> {
                            int weightGrams = (int) Math.max(100, Math.round(item.getWeightKg() * 1000));
                            var pkg = new kg.example.levantee.service.shipment.cdek.model.CdekTariffRequest.Package(
                                    weightGrams,
                                    (int) Math.max(1, item.getLengthCm()),
                                    (int) Math.max(1, item.getWidthCm()),
                                    (int) Math.max(1, item.getHeightCm())
                            );
                            return java.util.Collections.nCopies(item.getQuantity(), pkg).stream();
                        })
                        .toList();

        List<kg.example.levantee.service.shipment.cdek.model.CdekTariffRequest.Service> services =
                request.getInsuranceAmount() != null && request.getInsuranceAmount() > 0
                        ? List.of(new kg.example.levantee.service.shipment.cdek.model.CdekTariffRequest.Service(
                                "INSURANCE", String.valueOf(request.getInsuranceAmount().intValue())))
                        : null;

        CdekOrderApiRequest orderRequest = CdekOrderApiRequest.builder()
                .tariffCode(request.getTariffCode())
                .fromLocation(CdekOrderApiRequest.Location.ofCity(request.getFromCityCode()))
                .toLocation(toLocation)
                .recipient(recipient)
                .packages(packages)
                .services(services)
                .build();

        CdekOrderApiResponse response = cdekClient.createOrder(orderRequest);

        String status = (response.getRequests() != null && !response.getRequests().isEmpty())
                ? response.getRequests().get(0).getState()
                : "UNKNOWN";

        return new CdekCreateOrderResponse(
                response.getEntity().getUuid(),
                response.getEntity().getCdekNumber(),
                status
        );
    }
}