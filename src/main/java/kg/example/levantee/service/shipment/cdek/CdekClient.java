package kg.example.levantee.service.shipment.cdek;

import kg.example.levantee.service.shipment.cdek.model.CdekCity;
import kg.example.levantee.service.shipment.cdek.model.CdekDeliveryPoint;
import kg.example.levantee.service.shipment.cdek.model.CdekOrderApiRequest;
import kg.example.levantee.service.shipment.cdek.model.CdekOrderApiResponse;
import kg.example.levantee.service.shipment.cdek.model.CdekTariffListResponse;
import kg.example.levantee.service.shipment.cdek.model.CdekTariffRequest;
import kg.example.levantee.service.shipment.cdek.model.CdekTariffResponse;
import kg.example.levantee.service.shipment.cdek.model.CdekTokenResponse;
import kg.example.levantee.dto.shipmentDto.ShipmentPackage;
import kg.example.levantee.dto.shipmentDto.ShipmentParams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CdekClient {

    private final RestTemplate restTemplate;
    private final CdekProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String cachedToken;
    private LocalDateTime tokenExpiresAt;

    private synchronized String getToken() {
        if (properties.getClientId() == null || properties.getClientId().isBlank()
                || properties.getClientSecret() == null || properties.getClientSecret().isBlank()) {
            throw new IllegalStateException(
                    "CDEK credentials не настроены. Задайте CDEK_CLIENT_ID и CDEK_CLIENT_SECRET в переменных окружения");
        }
        log.debug("CDEK clientId='{}'", properties.getClientId());
        if (cachedToken != null && LocalDateTime.now().isBefore(tokenExpiresAt)) {
            return cachedToken;
        }

        log.info("Получение нового токена CDEK");

        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> tokenBody = new LinkedMultiValueMap<>();
        tokenBody.add("grant_type", "client_credentials");
        tokenBody.add("client_id", properties.getClientId());
        tokenBody.add("client_secret", properties.getClientSecret());

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenBody, tokenHeaders);

        CdekTokenResponse response = restTemplate.postForObject(
                properties.getUrl() + "/oauth/token",
                tokenRequest,
                CdekTokenResponse.class
        );

        if (response == null || response.getAccessToken() == null) {
            throw new IllegalStateException("Не удалось получить токен CDEK");
        }

        cachedToken = response.getAccessToken();
        tokenExpiresAt = LocalDateTime.now().plusSeconds(response.getExpiresIn() - 60);

        log.info("Токен CDEK получен, действителен до: {}", tokenExpiresAt);
        return cachedToken;
    }

    public double calculateTariff(List<ShipmentPackage> items, ShipmentParams params) {
        int fromCityCode = properties.getFromCityCode();
        int toCityCode   = params.getToCityCode();
        int tariffCode   = params.getTariffCode()   > 0 ? params.getTariffCode()   : properties.getTariffCode();

        if (toCityCode <= 0) {
            throw new IllegalArgumentException("Укажите код города получения");
        }

        double totalWeightKg = items.stream()
                .mapToDouble(p -> p.getWeightKg() * p.getQuantity())
                .sum();

        if (totalWeightKg > 500.0) {
            throw new IllegalArgumentException(
                    "Вес заказа %.1f кг превышает максимально допустимый лимит CDEK (500 кг)".formatted(totalWeightKg));
        }

        try {
            String token = getToken();

            List<CdekTariffRequest.Package> packages = items.stream()
                    .flatMap(item -> {
                        int weightGrams = (int) Math.max(100, Math.round(item.getWeightKg() * 1000));
                        CdekTariffRequest.Package pkg = new CdekTariffRequest.Package(
                                weightGrams,
                                (int) Math.max(1, item.getLengthCm()),
                                (int) Math.max(1, item.getWidthCm()),
                                (int) Math.max(1, item.getHeightCm()));
                        return java.util.Collections.nCopies(item.getQuantity(), pkg).stream();
                    })
                    .toList();

            CdekTariffRequest request = CdekTariffRequest.builder()
                    .tariffCode(tariffCode)
                    .fromLocation(new CdekTariffRequest.Location(fromCityCode))
                    .toLocation(new CdekTariffRequest.Location(toCityCode))
                    .packages(packages)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<CdekTariffRequest> entity = new HttpEntity<>(request, headers);

            log.info("Расчёт тарифа CDEK: вес={}кг, мест={}, тариф={}, откуда={}, куда={}",
                    totalWeightKg, packages.size(), tariffCode, fromCityCode, toCityCode);

            try {
                log.info("CDEK запрос JSON: {}", objectMapper.writeValueAsString(request));
            } catch (JsonProcessingException ignored) {}

            CdekTariffResponse response = restTemplate.postForObject(
                    properties.getUrl() + "/calculator/tariff",
                    entity,
                    CdekTariffResponse.class
            );

            if (response == null) {
                throw new IllegalStateException("CDEK не вернул ответ на расчёт тарифа");
            }

            log.info("Тариф CDEK: {} {}, срок {}-{} дней",
                    response.getTotalSum(), response.getCurrency(),
                    response.getPeriodMin(), response.getPeriodMax());

            return response.getTotalSum();

        } catch (RestClientException e) {
            log.error("CDEK API недоступен: {}", e.getMessage());
            throw new IllegalStateException("Сервис CDEK временно недоступен, попробуйте позже");
        }
    }

    public CdekTariffListResponse getTariffList(int toCityCode) {
        int resolvedFrom = properties.getFromCityCode();
        int resolvedTo   = toCityCode;

        try {
            String token = getToken();

            CdekTariffRequest request = CdekTariffRequest.builder()
                    .fromLocation(new CdekTariffRequest.Location(resolvedFrom))
                    .toLocation(new CdekTariffRequest.Location(resolvedTo))
                    .packages(List.of(new CdekTariffRequest.Package(1000, 10, 10, 10)))
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<CdekTariffRequest> entity = new HttpEntity<>(request, headers);

            log.info("Запрос тарифного листа CDEK: откуда={}, куда={}", resolvedFrom, resolvedTo);

            CdekTariffListResponse response = restTemplate.postForObject(
                    properties.getUrl() + "/calculator/tarifflist",
                    entity,
                    CdekTariffListResponse.class
            );

            if (response == null || response.getTariffCodes() == null) {
                throw new IllegalStateException("CDEK не вернул список тарифов");
            }

            log.info("Получено тарифов от CDEK: {}", response.getTariffCodes().size());
            return response;

        } catch (RestClientException e) {
            log.error("CDEK API недоступен при запросе тарифов: {}", e.getMessage());
            throw new IllegalStateException("Сервис CDEK временно недоступен, попробуйте позже");
        }
    }

    // Шаг 2: Список ПВЗ (только если пользователь выбрал WAREHOUSE)
    public List<CdekDeliveryPoint> getDeliveryPoints(int cityCode) {
        try {
            String token = getToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.info("Запрос списка ПВЗ CDEK для города: {}", cityCode);

            CdekDeliveryPoint[] response = restTemplate.exchange(
                    properties.getUrl() + "/deliverypoints?city_code=" + cityCode,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    CdekDeliveryPoint[].class
            ).getBody();

            if (response == null) {
                throw new IllegalStateException("CDEK не вернул список ПВЗ");
            }

            log.info("Получено ПВЗ от CDEK: {}", response.length);
            return Arrays.asList(response);

        } catch (RestClientException e) {
            log.error("CDEK API недоступен при запросе ПВЗ: {}", e.getMessage());
            throw new IllegalStateException("Сервис CDEK временно недоступен, попробуйте позже");
        }
    }

    // Шаг 4: Расчёт конкретного тарифа (с опциональной страховкой)
    public CdekTariffResponse calculateSingleTariff(List<ShipmentPackage> items, ShipmentParams params, Double insuranceAmount) {
        int fromCityCode = properties.getFromCityCode();
        int toCityCode   = params.getToCityCode();
        int tariffCode   = params.getTariffCode()   > 0 ? params.getTariffCode()   : properties.getTariffCode();

        try {
            String token = getToken();

            List<CdekTariffRequest.Package> packages = items.stream()
                    .flatMap(item -> {
                        int weightGrams = (int) Math.max(100, Math.round(item.getWeightKg() * 1000));
                        CdekTariffRequest.Package pkg = new CdekTariffRequest.Package(
                                weightGrams,
                                (int) Math.max(1, item.getLengthCm()),
                                (int) Math.max(1, item.getWidthCm()),
                                (int) Math.max(1, item.getHeightCm()));
                        return java.util.Collections.nCopies(item.getQuantity(), pkg).stream();
                    })
                    .toList();

            List<CdekTariffRequest.Service> services = insuranceAmount != null && insuranceAmount > 0
                    ? List.of(new CdekTariffRequest.Service("INSURANCE", String.valueOf(insuranceAmount.intValue())))
                    : null;

            CdekTariffRequest request = CdekTariffRequest.builder()
                    .tariffCode(tariffCode)
                    .fromLocation(new CdekTariffRequest.Location(fromCityCode))
                    .toLocation(new CdekTariffRequest.Location(toCityCode))
                    .packages(packages)
                    .services(services)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<CdekTariffRequest> entity = new HttpEntity<>(request, headers);

            log.info("Расчёт тарифа CDEK #{}, страховка: {}", tariffCode, insuranceAmount);

            CdekTariffResponse response = restTemplate.postForObject(
                    properties.getUrl() + "/calculator/tariff",
                    entity,
                    CdekTariffResponse.class
            );

            if (response == null) {
                throw new IllegalStateException("CDEK не вернул ответ на расчёт тарифа");
            }

            return response;

        } catch (RestClientException e) {
            log.error("CDEK API недоступен: {}", e.getMessage());
            throw new IllegalStateException("Сервис CDEK временно недоступен, попробуйте позже");
        }
    }

    // Шаг 5: Создание заказа
    public CdekOrderApiResponse createOrder(CdekOrderApiRequest orderRequest) {
        try {
            String token = getToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<CdekOrderApiRequest> entity = new HttpEntity<>(orderRequest, headers);

            log.info("Создание заказа CDEK, тариф: {}", orderRequest.getTariffCode());

            CdekOrderApiResponse response = restTemplate.postForObject(
                    properties.getUrl() + "/orders",
                    entity,
                    CdekOrderApiResponse.class
            );

            if (response == null || response.getEntity() == null) {
                throw new IllegalStateException("CDEK не вернул ответ при создании заказа");
            }

            log.info("Заказ CDEK создан, UUID: {}", response.getEntity().getUuid());
            return response;

        } catch (RestClientException e) {
            log.error("CDEK API недоступен при создании заказа: {}", e.getMessage());
            throw new IllegalStateException("Сервис CDEK временно недоступен, попробуйте позже");
        }
    }

    // Поиск городов CDEK по названию
    public List<CdekCity> searchCities(String name) {
        try {
            String token = getToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.info("Поиск городов CDEK: '{}'", name);

            CdekCity[] response = restTemplate.exchange(
                    properties.getUrl() + "/location/cities?city=" + name,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    CdekCity[].class
            ).getBody();

            if (response == null) {
                return List.of();
            }

            return Arrays.asList(response);

        } catch (RestClientException e) {
            log.error("CDEK API недоступен при поиске городов: {}", e.getMessage());
            throw new IllegalStateException("Сервис CDEK временно недоступен, попробуйте позже");
        }
    }
}