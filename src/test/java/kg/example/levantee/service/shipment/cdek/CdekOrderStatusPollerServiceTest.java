package kg.example.levantee.service.shipment.cdek;

import kg.example.levantee.model.entity.shipment.Shipment;
import kg.example.levantee.repository.ShipmentRepository;
import kg.example.levantee.service.shipment.cdek.client.CdekClient;
import kg.example.levantee.service.shipment.cdek.model.CdekOrderApiResponse;
import kg.example.levantee.service.shipment.cdek.service.CdekOrderStatusPollerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CdekOrderStatusPollerServiceTest {

    @Mock
    CdekClient cdekClient;
    @Mock ShipmentRepository shipmentRepository;

    @InjectMocks
    CdekOrderStatusPollerService poller;

    // ── SUCCESSFUL ────────────────────────────────────────────────────────────

    @Test
    void pollAcceptedOrders_successfulState_updatesShipment() {
        Shipment shipment = shipmentWithUuid("uuid-ok");

        when(shipmentRepository.findAllByCdekRequestStatus("ACCEPTED")).thenReturn(List.of(shipment));
        when(cdekClient.getOrderStatus("uuid-ok")).thenReturn(buildResponse("SUCCESSFUL", "Создан", "CDEK-12345"));

        poller.pollAcceptedOrders();

        ArgumentCaptor<Shipment> captor = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepository).save(captor.capture());

        Shipment saved = captor.getValue();
        assertThat(saved.getCdekRequestStatus()).isEqualTo("SUCCESSFUL");
        assertThat(saved.getCdekNumber()).isEqualTo("CDEK-12345");
        assertThat(saved.getCdekStatus()).isEqualTo("Создан");
        assertThat(saved.getCdekPollAttempts()).isEqualTo(1);
    }

    // ── INVALID ───────────────────────────────────────────────────────────────

    @Test
    void pollAcceptedOrders_invalidState_marksInvalid() {
        Shipment shipment = shipmentWithUuid("uuid-bad");

        when(shipmentRepository.findAllByCdekRequestStatus("ACCEPTED")).thenReturn(List.of(shipment));
        when(cdekClient.getOrderStatus("uuid-bad")).thenReturn(buildResponseInvalid("ERR001", "Неверный тариф"));

        poller.pollAcceptedOrders();

        ArgumentCaptor<Shipment> captor = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepository).save(captor.capture());

        assertThat(captor.getValue().getCdekRequestStatus()).isEqualTo("INVALID");
    }

    // ── Превышен лимит попыток ────────────────────────────────────────────────

    @Test
    void pollAcceptedOrders_maxAttemptsReached_stopsPolling() {
        Shipment shipment = shipmentWithUuid("uuid-timeout");
        shipment.setCdekPollAttempts(10); // уже на максимуме

        when(shipmentRepository.findAllByCdekRequestStatus("ACCEPTED")).thenReturn(List.of(shipment));

        poller.pollAcceptedOrders();

        verifyNoInteractions(cdekClient); // API не вызывается
        ArgumentCaptor<Shipment> captor = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepository).save(captor.capture());
        assertThat(captor.getValue().getCdekRequestStatus()).isEqualTo("POLL_TIMEOUT");
    }

    // ── Ошибка при вызове API ─────────────────────────────────────────────────

    @Test
    void pollAcceptedOrders_cdekApiError_savesIncrementedAttempts() {
        Shipment shipment = shipmentWithUuid("uuid-err");

        when(shipmentRepository.findAllByCdekRequestStatus("ACCEPTED")).thenReturn(List.of(shipment));
        when(cdekClient.getOrderStatus("uuid-err")).thenThrow(new IllegalStateException("Timeout"));

        poller.pollAcceptedOrders();

        ArgumentCaptor<Shipment> captor = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepository).save(captor.capture());

        assertThat(captor.getValue().getCdekPollAttempts()).isEqualTo(1);
        assertThat(captor.getValue().getCdekRequestStatus()).isEqualTo("ACCEPTED"); // не изменился
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Shipment shipmentWithUuid(String uuid) {
        Shipment s = new Shipment();
        s.setCdekUuid(uuid);
        s.setCdekRequestStatus("ACCEPTED");
        s.setCdekPollAttempts(0);
        return s;
    }

    private CdekOrderApiResponse buildResponse(String state, String statusName, String cdekNumber) {
        CdekOrderApiResponse.Request req = new CdekOrderApiResponse.Request();
        req.setState(state);

        CdekOrderApiResponse.Entity entity = new CdekOrderApiResponse.Entity();
        entity.setCdekNumber(cdekNumber);

        CdekOrderApiResponse.Entity.Status status = new CdekOrderApiResponse.Entity.Status();
        status.setName(statusName);
        entity.setStatuses(List.of(status));

        CdekOrderApiResponse response = new CdekOrderApiResponse();
        response.setRequests(List.of(req));
        response.setEntity(entity);
        return response;
    }

    private CdekOrderApiResponse buildResponseInvalid(String errCode, String errMsg) {
        CdekOrderApiResponse.Request.Error error = new CdekOrderApiResponse.Request.Error();
        error.setCode(errCode);
        error.setMessage(errMsg);

        CdekOrderApiResponse.Request req = new CdekOrderApiResponse.Request();
        req.setState("INVALID");
        req.setErrors(List.of(error));

        CdekOrderApiResponse response = new CdekOrderApiResponse();
        response.setRequests(List.of(req));
        response.setEntity(new CdekOrderApiResponse.Entity());
        return response;
    }
}