package kg.example.levantee.service.shipment.cdek.client;

import kg.example.levantee.service.shipment.cdek.model.CdekProperties;
import kg.example.levantee.service.shipment.cdek.model.CdekTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class CdekAuthService {

    private static final String TOKEN_KEY = "cdek:token";

    private final CdekProperties properties;
    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate;

    public String getToken() {
        if (properties.getClientId() == null || properties.getClientId().isBlank()
                || properties.getClientSecret() == null || properties.getClientSecret().isBlank()) {
            throw new IllegalStateException(
                    "CDEK credentials не настроены. Задайте CDEK_CLIENT_ID и CDEK_CLIENT_SECRET");
        }

        String cached = redisTemplate.opsForValue().get(TOKEN_KEY);
        if (cached != null) {
            return cached;
        }

        log.info("Получение нового токена CDEK");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", properties.getClientId());
        body.add("client_secret", properties.getClientSecret());

        CdekTokenResponse response = restTemplate.postForObject(
                properties.getUrl() + "/oauth/token",
                new HttpEntity<>(body, headers),
                CdekTokenResponse.class
        );

        if (response == null || response.getAccessToken() == null) {
            throw new IllegalStateException("Не удалось получить токен CDEK");
        }

        long ttlSeconds = Math.max(60, response.getExpiresIn() - 60);
        redisTemplate.opsForValue().set(TOKEN_KEY, response.getAccessToken(), Duration.ofSeconds(ttlSeconds));

        log.info("Токен CDEK получен, TTL={} сек", ttlSeconds);
        return response.getAccessToken();
    }
}