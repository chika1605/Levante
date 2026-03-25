package kg.example.levantee.service.shipment.cdek;

import kg.example.levantee.dto.cdekDto.CdekCreateOrderRequest;
import kg.example.levantee.dto.cdekDto.CdekCreateOrderResponse;
import kg.example.levantee.dto.shipmentDto.ShipmentPackage;
import kg.example.levantee.service.shipment.cdek.client.CdekClient;
import kg.example.levantee.service.shipment.cdek.model.CdekOrderApiRequest;
import kg.example.levantee.service.shipment.cdek.model.CdekOrderApiResponse;
import kg.example.levantee.service.shipment.cdek.model.CdekProperties;
import kg.example.levantee.service.shipment.cdek.service.CdekService;
import org.junit.jupiter.api.BeforeEach;
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
class CdekServiceTest {

    @Mock
    CdekClient cdekClient;
    @Mock
    CdekProperties cdekProperties;

    @InjectMocks
    CdekService service;

    @BeforeEach
    void setup() {
        when(cdekProperties.getFromCityCode()).thenReturn(5444);
    }

    // ── createOrder — успешная регистрация ────────────────────────────────────

    @Test
    void createOrder_buildsCorrectRequest_andReturnsUuid() {
        CdekOrderApiResponse response = buildCdekResponse("uuid-xyz", "ACCEPTED");
        when(cdekClient.createOrder(any())).thenReturn(response);

        CdekCreateOrderRequest request = buildRequest();
        CdekCreateOrderResponse result = service.createOrder(request);

        assertThat(result.getUuid()).isEqualTo("uuid-xyz");
        assertThat(result.getStatus()).isEqualTo("ACCEPTED");

        ArgumentCaptor<CdekOrderApiRequest> captor = ArgumentCaptor.forClass(CdekOrderApiRequest.class);
        verify(cdekClient).createOrder(captor.capture());

        CdekOrderApiRequest sent = captor.getValue();
        assertThat(sent.getType()).isEqualTo(2);
        assertThat(sent.getTariffCode()).isEqualTo(136);
        assertThat(sent.getRecipient().getName()).isEqualTo("Иван Иванов");
        assertThat(sent.getRecipient().getPhones()).hasSize(1);
        assertThat(sent.getRecipient().getPhones().get(0).getNumber()).isEqualTo("+996700000000");

        // Каждый пакет должен иметь number
        assertThat(sent.getPackages()).isNotEmpty();
        sent.getPackages().forEach(p -> assertThat(p.getNumber()).isNotBlank());

        // from_location использует fromCityCode из properties
        assertThat(sent.getFromLocation().getCode()).isEqualTo(5444);
        // to_location содержит toCityCode и адрес
        assertThat(sent.getToLocation().getCode()).isEqualTo(270);
        assertThat(sent.getToLocation().getAddress()).isEqualTo("ул. Ленина 1");
    }

    // ── createOrder — пакеты нумеруются последовательно ──────────────────────

    @Test
    void createOrder_multiplePackages_numberedSequentially() {
        CdekOrderApiResponse response = buildCdekResponse("uuid-m", "ACCEPTED");
        when(cdekClient.createOrder(any())).thenReturn(response);

        CdekCreateOrderRequest request = buildRequest();
        // 2 позиции, qty=1 каждая
        request.setPackages(List.of(
                new ShipmentPackage(1.0, 20, 15, 10, 1),
                new ShipmentPackage(0.5, 10, 10, 5, 2)
        ));

        service.createOrder(request);

        ArgumentCaptor<CdekOrderApiRequest> captor = ArgumentCaptor.forClass(CdekOrderApiRequest.class);
        verify(cdekClient).createOrder(captor.capture());

        List<CdekOrderApiRequest.Package> packages = captor.getValue().getPackages();
        assertThat(packages).hasSize(3); // 1 + 2
        assertThat(packages.get(0).getNumber()).isEqualTo("1");
        assertThat(packages.get(1).getNumber()).isEqualTo("2");
        assertThat(packages.get(2).getNumber()).isEqualTo("3");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CdekCreateOrderRequest buildRequest() {
        CdekCreateOrderRequest r = new CdekCreateOrderRequest();
        r.setTariffCode(136);
        r.setFromCityCode(0); // будет взят из properties
        r.setToCityCode(270);
        r.setToAddress("ул. Ленина 1");
        r.setRecipientName("Иван Иванов");
        r.setRecipientPhone("+996700000000");
        r.setPackages(List.of(new ShipmentPackage(1.5, 30, 20, 10, 1)));
        return r;
    }

    private CdekOrderApiResponse buildCdekResponse(String uuid, String state) {
        CdekOrderApiResponse.Request req = new CdekOrderApiResponse.Request();
        req.setState(state);

        CdekOrderApiResponse.Entity entity = new CdekOrderApiResponse.Entity();
        entity.setUuid(uuid);

        CdekOrderApiResponse response = new CdekOrderApiResponse();
        response.setRequests(List.of(req));
        response.setEntity(entity);
        return response;
    }
}