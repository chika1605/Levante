package kg.example.levantee.service.shipment.cdek.client;

import kg.example.levantee.service.shipment.cdek.model.CdekProperties;
import kg.example.levantee.service.shipment.cdek.model.CdekCity;
import kg.example.levantee.service.shipment.cdek.model.CdekOrderApiRequest;
import kg.example.levantee.service.shipment.cdek.model.CdekOrderApiResponse;
import kg.example.levantee.service.shipment.cdek.model.CdekTariffListResponse;
import kg.example.levantee.service.shipment.cdek.model.CdekTariffRequest;
import kg.example.levantee.service.shipment.cdek.model.CdekTariffResponse;
import kg.example.levantee.dto.shipmentDto.ShipmentPackage;
import kg.example.levantee.dto.shipmentDto.ShipmentParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class CdekClient {

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1_000;

    private final RestTemplate restTemplate;
    private final CdekProperties properties;
    private final CdekAuthService cdekAuthService;


    private <T> T withRetry(String operation, Supplier<T> call) {
        RestClientException lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return call.get();
            } catch (HttpClientErrorException e) {
                log.error("CDEK [{}] ошибка {}: {}", operation, e.getStatusCode(), e.getResponseBodyAsString());
                throw new IllegalStateException("CDEK вернул ошибку: " + e.getResponseBodyAsString(), e);
            } catch (RestClientException e) {
                lastException = e;
                if (attempt < MAX_RETRIES) {
                    log.warn("CDEK [{}] попытка {}/{} не удалась: {}", operation, attempt, MAX_RETRIES, e.getMessage());
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        log.error("CDEK [{}] недоступен после {} попыток", operation, MAX_RETRIES);
        throw new IllegalStateException("Сервис CDEK временно недоступен, попробуйте позже", lastException);
    }


    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(cdekAuthService.getToken());
        return h;
    }

    private HttpHeaders authJsonHeaders() {
        HttpHeaders h = authHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }


    public double calculateTariff(List<ShipmentPackage> items, ShipmentParams params) {
        int fromCityCode = properties.getFromCityCode();
        int toCityCode   = params.getToCityCode();
        int tariffCode   = params.getTariffCode() > 0 ? params.getTariffCode() : properties.getTariffCode();

        if (toCityCode <= 0) throw new IllegalArgumentException("Укажите код города получения");

        double totalWeightKg = items.stream().mapToDouble(p -> p.getWeightKg() * p.getQuantity()).sum();
        if (totalWeightKg > 500.0) {
            throw new IllegalArgumentException(
                    "Вес заказа %.1f кг превышает максимально допустимый лимит CDEK (500 кг)".formatted(totalWeightKg));
        }

        List<CdekTariffRequest.Package> packages = buildTariffPackages(items);

        CdekTariffRequest request = CdekTariffRequest.builder()
                .tariffCode(tariffCode)
                .fromLocation(new CdekTariffRequest.Location(fromCityCode))
                .toLocation(new CdekTariffRequest.Location(toCityCode))
                .packages(packages)
                .build();

        log.info("Расчёт тарифа CDEK: вес={}кг, тариф={}", totalWeightKg, tariffCode);

        CdekTariffResponse response = withRetry("calculateTariff", () ->
                restTemplate.postForObject(
                        properties.getUrl() + "/calculator/tariff",
                        new HttpEntity<>(request, authJsonHeaders()),
                        CdekTariffResponse.class));

        if (response == null) throw new IllegalStateException("CDEK не вернул ответ на расчёт тарифа");
        return response.getTotalSum();
    }


    public CdekTariffListResponse getTariffList(int toCityCode, List<ShipmentPackage> packages) {
        List<CdekTariffRequest.Package> pkgs = (packages != null && !packages.isEmpty())
                ? buildTariffPackages(packages)
                : List.of(new CdekTariffRequest.Package(1000, 10, 10, 10));

        CdekTariffRequest request = CdekTariffRequest.builder()
                .fromLocation(new CdekTariffRequest.Location(properties.getFromCityCode()))
                .toLocation(new CdekTariffRequest.Location(toCityCode))
                .packages(pkgs)
                .build();

        CdekTariffListResponse response = withRetry("getTariffList", () ->
                restTemplate.postForObject(
                        properties.getUrl() + "/calculator/tarifflist",
                        new HttpEntity<>(request, authJsonHeaders()),
                        CdekTariffListResponse.class));

        if (response == null || response.getTariffCodes() == null) {
            throw new IllegalStateException("CDEK не вернул список тарифов");
        }
        return response;
    }


    public List<CdekCity> searchCities(String name) {
        CdekCity[] response = withRetry("searchCities", () ->
                restTemplate.exchange(
                        properties.getUrl() + "/location/cities?city=" + name,
                        HttpMethod.GET,
                        new HttpEntity<>(authHeaders()),
                        CdekCity[].class).getBody());

        return response != null ? Arrays.asList(response) : List.of();
    }


    public CdekOrderApiResponse createOrder(CdekOrderApiRequest orderRequest) {
        log.info("Создание заказа CDEK, тариф: {}", orderRequest.getTariffCode());

        CdekOrderApiResponse response = withRetry("createOrder", () ->
                restTemplate.postForObject(
                        properties.getUrl() + "/orders",
                        new HttpEntity<>(orderRequest, authJsonHeaders()),
                        CdekOrderApiResponse.class));

        if (response == null || response.getEntity() == null) {
            throw new IllegalStateException("CDEK не вернул ответ при создании заказа");
        }

        String state = extractState(response);
        if ("INVALID".equals(state) && response.getRequests() != null) {
            response.getRequests().stream()
                    .filter(r -> r.getErrors() != null)
                    .flatMap(r -> r.getErrors().stream())
                    .forEach(e -> log.error("CDEK ошибка создания заказа: [{}] {}", e.getCode(), e.getMessage()));
            throw new IllegalStateException("CDEK отклонил заказ: " +
                    response.getRequests().get(0).getErrors().get(0).getMessage());
        }

        log.info("Заказ CDEK uuid={}, status={}", response.getEntity().getUuid(), state);
        return response;
    }

    public CdekOrderApiResponse getOrderStatus(String uuid) {
        log.info("Проверка статуса CDEK заказа uuid={}", uuid);

        CdekOrderApiResponse response = withRetry("getOrderStatus", () ->
                restTemplate.exchange(
                        properties.getUrl() + "/orders/" + uuid,
                        HttpMethod.GET,
                        new HttpEntity<>(authHeaders()),
                        CdekOrderApiResponse.class).getBody());

        if (response == null) throw new IllegalStateException("CDEK не вернул статус для uuid=" + uuid);
        return response;
    }

    private List<CdekTariffRequest.Package> buildTariffPackages(List<ShipmentPackage> items) {
        return items.stream()
                .flatMap(item -> {
                    int wg = (int) Math.max(100, Math.round(item.getWeightKg() * 1000));
                    var pkg = new CdekTariffRequest.Package(wg,
                            (int) Math.max(1, item.getLengthCm()),
                            (int) Math.max(1, item.getWidthCm()),
                            (int) Math.max(1, item.getHeightCm()));
                    return Collections.nCopies(item.getQuantity(), pkg).stream();
                })
                .toList();
    }

    private String extractState(CdekOrderApiResponse response) {
        if (response.getRequests() != null && !response.getRequests().isEmpty()) {
            return response.getRequests().get(0).getState();
        }
        return "UNKNOWN";
    }
}