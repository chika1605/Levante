package kg.example.levantee.service.shipment.cdek;

import kg.example.levantee.service.shipment.cdek.dto.CdekTariffRequest;
import kg.example.levantee.service.shipment.cdek.dto.CdekTariffResponse;
import kg.example.levantee.service.shipment.cdek.dto.CdekTokenResponse;
import kg.example.levantee.service.shipment.dto.CdekParams;
import kg.example.levantee.service.shipment.dto.ShipmentPackage;

import java.util.Set;
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
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CdekClient {

    private final RestTemplate restTemplate;
    private final CdekProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Set<Integer> SUPPORTED_TARIFFS = Set.of(
            62, 63, 136, 137, 138, 139, 233, 291
    );

    private String cachedToken;
    private LocalDateTime tokenExpiresAt;

    // Получаем токен и кешируем до истечения срока
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

        // CDEK требует credentials в теле как form-urlencoded, не в query параметрах
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
        // минус 60 секунд — чтобы не словить просроченный токен
        tokenExpiresAt = LocalDateTime.now().plusSeconds(response.getExpiresIn() - 60);

        log.info("Токен CDEK получен, действителен до: {}", tokenExpiresAt);
        return cachedToken;
    }

    public double calculateTariff(List<ShipmentPackage> items, CdekParams params) {
        int fromCityCode = params.getFromCityCode() > 0 ? params.getFromCityCode() : properties.getFromCityCode();
        int toCityCode   = params.getToCityCode()   > 0 ? params.getToCityCode()   : properties.getToCityCode();
        int tariffCode   = params.getTariffCode()   > 0 ? params.getTariffCode()   : properties.getTariffCode();

        if (!SUPPORTED_TARIFFS.contains(tariffCode)) {
            throw new IllegalArgumentException(
                    "Тариф %d не поддерживается. Допустимые: %s".formatted(tariffCode, SUPPORTED_TARIFFS));
        }
        if (fromCityCode <= 0) {
            throw new IllegalArgumentException("Код города отправки должен быть положительным числом");
        }
        if (toCityCode <= 0) {
            throw new IllegalArgumentException("Код города получения должен быть положительным числом");
        }

        double totalWeightKg = items.stream()
                .mapToDouble(p -> p.getWeightKg() * p.getQuantity())
                .sum();

        if (totalWeightKg > 500.0) {
            throw new IllegalArgumentException(
                    "Вес заказа %.1f кг превышает максимально допустимый лимит CDEK (500 кг)".formatted(totalWeightKg));
        }

        // Если выбран лёгкий тариф (136-139) и вес > 30кг — автоматически переключаем на тариф 62
        if (Set.of(136, 137, 138, 139).contains(tariffCode) && totalWeightKg > 30.0) {
            log.warn("Тариф {} поддерживает до 30кг, вес {}кг — автоматически используется тариф 62", tariffCode, totalWeightKg);
            tariffCode = 62;
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
}