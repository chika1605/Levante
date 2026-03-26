package kg.example.levantee.service.shipment.cdek;

import kg.example.levantee.model.entity.shipment.Shipment;
import kg.example.levantee.repository.ShipmentRepository;
import kg.example.levantee.service.shipment.cdek.client.CdekClient;
import kg.example.levantee.service.shipment.cdek.model.CdekOrderApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CdekOrderStatusPollerService {

    private static final int MAX_POLL_ATTEMPTS = 30;

    private final CdekClient cdekClient;
    private final ShipmentRepository shipmentRepository;

    @Scheduled(fixedDelay = 30_000)
    public void pollAcceptedOrders() {
        List<Shipment> pending = shipmentRepository.findAllByCdekRequestStatus("ACCEPTED");

        if (pending.isEmpty()) return;

        log.info("Поллинг статуса: {} CDEK-заказов в ACCEPTED", pending.size());

        for (Shipment shipment : pending) {
            if (shipment.getCdekPollAttempts() >= MAX_POLL_ATTEMPTS) {
                log.warn("Shipment #{} cdekUuid={} превысил лимит {} попыток, прекращаем опрос",
                        shipment.getId(), shipment.getCdekUuid(), MAX_POLL_ATTEMPTS);
                shipment.setCdekRequestStatus("POLL_TIMEOUT");
                shipmentRepository.save(shipment);
                continue;
            }

            shipment.setCdekPollAttempts(shipment.getCdekPollAttempts() + 1);

            try {
                CdekOrderApiResponse response = cdekClient.getOrderStatus(shipment.getCdekUuid());
                String newState = extractState(response);

                if ("SUCCESSFUL".equals(newState)) {
                    shipment.setCdekRequestStatus("SUCCESSFUL");
                    shipment.setCdekNumber(response.getEntity().getCdekNumber());
                    shipment.setCdekStatus(response.getEntity().getCurrentStatusName());
                    log.info("Shipment #{} cdekUuid={}: SUCCESSFUL, cdekNumber={}",
                            shipment.getId(), shipment.getCdekUuid(), shipment.getCdekNumber());

                } else if ("INVALID".equals(newState)) {
                    shipment.setCdekRequestStatus("INVALID");
                    logErrors(response, shipment.getCdekUuid());
                    log.error("Shipment #{} cdekUuid={}: INVALID — заказ не создан в CDEK",
                            shipment.getId(), shipment.getCdekUuid());

                } else {
                    log.info("Shipment #{} cdekUuid={}: статус всё ещё {}, попытка {}/{}",
                            shipment.getId(), shipment.getCdekUuid(), newState,
                            shipment.getCdekPollAttempts(), MAX_POLL_ATTEMPTS);
                }

            } catch (Exception e) {
                log.error("Ошибка при опросе CDEK uuid={}: {}", shipment.getCdekUuid(), e.getMessage());
            }

            shipmentRepository.save(shipment);
        }
    }

    private String extractState(CdekOrderApiResponse response) {
        if (response.getRequests() != null && !response.getRequests().isEmpty()) {
            return response.getRequests().get(0).getState();
        }
        return "UNKNOWN";
    }

    private void logErrors(CdekOrderApiResponse response, String uuid) {
        if (response.getRequests() == null) return;
        response.getRequests().forEach(r -> {
            if (r.getErrors() != null) {
                r.getErrors().forEach(e ->
                        log.error("CDEK ошибка uuid={}: [{}] {}", uuid, e.getCode(), e.getMessage()));
            }
        });
    }
}