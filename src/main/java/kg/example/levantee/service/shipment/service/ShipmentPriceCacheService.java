package kg.example.levantee.service.shipment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentPriceCacheService {

    private static final String KEY_PREFIX = "price:";
    private static final Duration TTL = Duration.ofHours(1);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(Long orderId, double deliveryPrice, double insurancePrice,
                     double totalPrice, double declaredValue, boolean insuranceEnabled, int tariffCode) {
        Entry entry = new Entry(deliveryPrice, insurancePrice, totalPrice, declaredValue, insuranceEnabled, tariffCode);
        try {
            String json = objectMapper.writeValueAsString(entry);
            redisTemplate.opsForValue().set(KEY_PREFIX + orderId, json, TTL);
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации цены для orderId={}: {}", orderId, e.getMessage());
        }
    }

    public Entry get(Long orderId) {
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + orderId);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, Entry.class);
        } catch (JsonProcessingException e) {
            log.error("Ошибка десериализации цены для orderId={}: {}", orderId, e.getMessage());
            return null;
        }
    }

    public void delete(Long orderId) {
        redisTemplate.delete(KEY_PREFIX + orderId);
    }

    @Data
    public static class Entry {
        private double deliveryPrice;
        private double insurancePrice;
        private double totalPrice;
        private double declaredValue;
        private boolean insuranceEnabled;
        private int tariffCode;


        public Entry(double deliveryPrice, double insurancePrice, double totalPrice,
                     double declaredValue, boolean insuranceEnabled, int tariffCode) {
            this.deliveryPrice = deliveryPrice;
            this.insurancePrice = insurancePrice;
            this.totalPrice = totalPrice;
            this.declaredValue = declaredValue;
            this.insuranceEnabled = insuranceEnabled;
            this.tariffCode = tariffCode;
        }
    }
}