package kg.example.levantee.service.shipment;

import kg.example.levantee.dto.shipmentDto.ShipmentPackage;
import kg.example.levantee.dto.shipmentDto.ShipmentRequest;
import kg.example.levantee.dto.shipmentDto.ShipmentResponse;
import kg.example.levantee.model.entity.order.Order;
import kg.example.levantee.model.entity.shipment.Shipment;
import kg.example.levantee.model.enums.order.OrderStatus;
import kg.example.levantee.model.entity.shipment.ShipmentPackageProjection;
import kg.example.levantee.repository.OrderRepository;
import kg.example.levantee.repository.ShipmentRepository;
import kg.example.levantee.service.shipment.cdek.CdekClient;
import kg.example.levantee.service.shipment.cdek.CdekProperties;
import kg.example.levantee.service.shipment.cdek.CdekService;
import kg.example.levantee.utils.exception.PriceChangedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kg.example.levantee.dto.cdekDto.CdekCreateOrderResponse;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock ShipmentRepository shipmentRepository;
    @Mock ShippingStrategyFactory strategyFactory;
    @Mock ShipmentPriceCacheService priceCacheService;
    @Mock CdekService cdekService;
    @Mock CdekClient cdekClient;
    @Mock CdekProperties cdekProperties;

    @InjectMocks ShipmentService service;

    private static final Long ORDER_ID = 1L;
    private static final String TARIFF_ID = "CDEK:136:270";
    private static final double ORDER_TOTAL = 5000.0;

    @BeforeEach
    void setup() {
        ShipmentPackageProjection pkg = mockProjection(1.0, 30.0, 20.0, 10.0, 1);
        when(orderRepository.existsById(ORDER_ID)).thenReturn(true);
        when(orderRepository.findShipmentPackages(ORDER_ID)).thenReturn(List.of(pkg));
        when(orderRepository.findTotalAmountByOrderId(ORDER_ID)).thenReturn(ORDER_TOTAL);

        ShippingStrategy strategy = mock(ShippingStrategy.class);
        when(strategyFactory.get("CDEK")).thenReturn(strategy);
        when(strategy.calculate(anyList(), any())).thenReturn(500.0);
    }

    // ── calculate без страховки ───────────────────────────────────────────────

    @Test
    void calculate_withoutInsurance_returnsZeroInsurance() {
        ShipmentResponse response = service.calculate(ORDER_ID, TARIFF_ID, false);

        assertThat(response.getDeliveryPrice()).isEqualTo(500.0);
        assertThat(response.getInsurancePrice()).isEqualTo(0.0);
        assertThat(response.getTotalPrice()).isEqualTo(500.0);
        assertThat(response.getDeclaredValue()).isEqualTo(ORDER_TOTAL);
        assertThat(response.isInsuranceEnabled()).isFalse();

        verify(priceCacheService).save(eq(ORDER_ID), eq(500.0), eq(0.0), eq(500.0), eq(ORDER_TOTAL), eq(false), eq(136));
    }

    // ── calculate со страховкой ───────────────────────────────────────────────

    @Test
    void calculate_withInsurance_calculatesInsurance() {
        ShipmentResponse response = service.calculate(ORDER_ID, TARIFF_ID, true);

        assertThat(response.getDeliveryPrice()).isEqualTo(500.0);
        assertThat(response.getInsurancePrice()).isEqualTo(50.0); // 5000 * 0.01
        assertThat(response.getTotalPrice()).isEqualTo(550.0);
        assertThat(response.getDeclaredValue()).isEqualTo(ORDER_TOTAL);
        assertThat(response.isInsuranceEnabled()).isTrue();

        verify(priceCacheService).save(eq(ORDER_ID), eq(500.0), eq(50.0), eq(550.0), eq(ORDER_TOTAL), eq(true), eq(136));
    }

    // ── POST /shipment — цены совпали ─────────────────────────────────────────

    @Test
    void createShipment_priceMatch_registersAndSaves() {
        Order order = new Order();
        order.setStatus(OrderStatus.CART);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.findTotalAmountByOrderId(ORDER_ID)).thenReturn(ORDER_TOTAL);

        // cached: insuranceEnabled=false, totalPrice=500
        ShipmentPriceCacheService.Entry cached = new ShipmentPriceCacheService.Entry(500.0, 0.0, 500.0, 0.0, false, 136);
        when(priceCacheService.get(ORDER_ID)).thenReturn(cached);

        CdekCreateOrderResponse cdekResp = new CdekCreateOrderResponse("uuid-123", null, "ACCEPTED");
        when(cdekService.createOrder(any())).thenReturn(cdekResp);
        when(shipmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cdekProperties.getFromCityCode()).thenReturn(5444);

        var result = service.createShipment(buildRequest());

        assertThat(result.getCdekUuid()).isEqualTo("uuid-123");
        assertThat(result.getCdekRequestStatus()).isEqualTo("ACCEPTED");
        assertThat(result.getCalculatedPrice()).isEqualTo(500.0);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);

        verify(priceCacheService).delete(ORDER_ID);
    }

    // ── POST /shipment — цена изменилась → 409 ────────────────────────────────

    @Test
    void createShipment_priceChanged_throws409() {
        Order order = new Order();
        order.setStatus(OrderStatus.CART);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.findTotalAmountByOrderId(ORDER_ID)).thenReturn(ORDER_TOTAL);

        // Кэш содержит старую цену 400, свежая цена = 500
        ShipmentPriceCacheService.Entry cached = new ShipmentPriceCacheService.Entry(400.0, 0.0, 400.0, 0.0, false, 136);
        when(priceCacheService.get(ORDER_ID)).thenReturn(cached);

        assertThatThrownBy(() -> service.createShipment(buildRequest()))
                .isInstanceOf(PriceChangedException.class)
                .satisfies(ex -> {
                    PriceChangedException pce = (PriceChangedException) ex;
                    assertThat(pce.getOldPrice()).isEqualTo(400.0);
                    assertThat(pce.getNewPrice()).isEqualTo(500.0);
                });

        // Redis обновляется с новой ценой
        verify(priceCacheService).save(eq(ORDER_ID), eq(500.0), eq(0.0), eq(500.0), eq(0.0), eq(false), eq(136));
        // CDEK не вызывается
        verifyNoInteractions(cdekService);
    }

    // ── Отмена — правильный статус ────────────────────────────────────────────

    @Test
    void cancelShipment_inCorrectStatus_callsCdekAndUpdatesOrder() {
        Order order = new Order();
        order.setStatus(OrderStatus.COMPLETED);

        Shipment shipment = new Shipment();
        shipment.setCdekUuid("uuid-del");
        shipment.setCdekStatus("Создан");
        shipment.setOrder(order);

        when(shipmentRepository.findByCdekUuid("uuid-del")).thenReturn(Optional.of(shipment));

        service.cancelShipment("uuid-del");

        verify(cdekClient).cancelOrder("uuid-del");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    // ── Отмена — неправильный статус ─────────────────────────────────────────

    @Test
    void cancelShipment_wrongStatus_throwsException() {
        Shipment shipment = new Shipment();
        shipment.setCdekUuid("uuid-x");
        shipment.setCdekStatus("В пути");
        shipment.setOrder(new Order());

        when(shipmentRepository.findByCdekUuid("uuid-x")).thenReturn(Optional.of(shipment));

        assertThatThrownBy(() -> service.cancelShipment("uuid-x"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Создан");

        verifyNoInteractions(cdekClient);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ShipmentRequest buildRequest() {
        ShipmentRequest r = new ShipmentRequest();
        r.setOrderId(ORDER_ID);
        r.setTariffId(TARIFF_ID);
        r.setRecipientName("Иван Иванов");
        r.setRecipientPhone("+996700000000");
        r.setDeliveryAddress("ул. Ленина 1");
        return r;
    }

    private ShipmentPackageProjection mockProjection(double w, double l, double wi, double h, int qty) {
        ShipmentPackageProjection p = mock(ShipmentPackageProjection.class);
        when(p.getWeightKg()).thenReturn(w);
        when(p.getLengthCm()).thenReturn(l);
        when(p.getWidthCm()).thenReturn(wi);
        when(p.getHeightCm()).thenReturn(h);
        when(p.getQuantity()).thenReturn(qty);
        return p;
    }
}