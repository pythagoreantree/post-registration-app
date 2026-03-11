package ru.post.PostRegistrationApp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.post.PostRegistrationApp.client.AddressServiceClient;
import ru.post.PostRegistrationApp.client.UserServiceClient;
import ru.post.PostRegistrationApp.domain.*;
import ru.post.PostRegistrationApp.dto.request.PostItemRequest;
import ru.post.PostRegistrationApp.dto.response.AddressValidationResponse;
import ru.post.PostRegistrationApp.dto.response.UserStatusResponse;
import ru.post.PostRegistrationApp.jpa.DraftShipmentRepository;
import ru.post.PostRegistrationApp.jpa.OutboxPaymentEventRepository;
import ru.post.PostRegistrationApp.service.PriceCalculationService;
import ru.post.PostRegistrationApp.service.RegistrationService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private DraftShipmentRepository draftShipmentRepository;

    @Mock
    private OutboxPaymentEventRepository outboxPaymentEventRepository;

    @Mock
    private PriceCalculationService priceCalculationService;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private AddressServiceClient addressServiceClient;

    @Mock
    private ExecutorService mongoExecutor;

    @InjectMocks
    private RegistrationService registrationService;

    @Captor
    private ArgumentCaptor<DraftShipment> draftShipmentCaptor;

    @Captor
    private ArgumentCaptor<OutboxPaymentEvent> outboxEventCaptor;

    private PostItemRequest request;
    private PostItemRequest.PostalPartyRequest sender;
    private PostItemRequest.PostalPartyRequest receiver;
    private UUID userId;

    @BeforeEach
    void setUp() {
        // Создаём отправителя
        sender = PostItemRequest.PostalPartyRequest.builder()
                .firstName("Григорий")
                .middleName("Валентинович")
                .lastName("Белов")
                .postalCode("946481")
                .city("Мытищи")
                .street("Восточная пр.")
                .house("212")
                .build();

        // Создаём получателя
        receiver = PostItemRequest.PostalPartyRequest.builder()
                .firstName("Евгений")
                .middleName("Егорович")
                .lastName("Дорофеев")
                .postalCode("947720")
                .city("Тверь")
                .street("улица Степная")
                .house("990")
                .building("1")
                .build();

        userId = UUID.randomUUID();

        request = PostItemRequest.builder()
                .type("LETTER")
                .userId(userId)
                .sender(sender)
                .receiver(receiver)
                .postOfficeCode("MOSCOW-045")
                .operatorId("OP-54321")
                .sourceSystem("MOBILE_APP")
                .acceptedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void validate_shouldReturnSuccessWhenAllServicesOk() {
        // given
        when(userServiceClient.getUserStatus(userId))
                .thenReturn(CompletableFuture.completedFuture(
                        UserStatusResponse.builder().active(true).build()));

        when(addressServiceClient.validateAddress(anyString(), eq(false)))
                .thenReturn(CompletableFuture.completedFuture(
                        AddressValidationResponse.builder().exists(true).build()));

        // when
        CompletableFuture<ValidationResult> result = registrationService.validate(request);

        // then
        ValidationResult validationResult = result.join();
        assertThat(validationResult.isValid()).isTrue();
        assertThat(validationResult.getMessage()).isNotBlank();

        verify(userServiceClient).getUserStatus(userId);
        verify(addressServiceClient, times(2)).validateAddress(anyString(), eq(false));
    }

    @Test
    void validate_shouldReturnErrorWhenUserInactive() {
        // given
        when(userServiceClient.getUserStatus(userId))
                .thenReturn(CompletableFuture.completedFuture(
                        UserStatusResponse.builder().active(false).build()));

        when(addressServiceClient.validateAddress(anyString(), eq(false)))
                .thenReturn(CompletableFuture.completedFuture(
                        AddressValidationResponse.builder().exists(true).build()));

        // when
        CompletableFuture<ValidationResult> result = registrationService.validate(request);

        // then
        ValidationResult validationResult = result.join();
        assertThat(validationResult.isValid()).isFalse();
        assertThat(validationResult.getMessage()).contains("Пользователь не активен");
    }

    @Test
    void validate_shouldReturnErrorWhenSenderAddressNotFound() {
        // given
        when(userServiceClient.getUserStatus(userId))
                .thenReturn(CompletableFuture.completedFuture(
                        UserStatusResponse.builder().active(true).build()));

        when(addressServiceClient.validateAddress(contains("Восточная"), eq(false)))
                .thenReturn(CompletableFuture.completedFuture(
                        AddressValidationResponse.builder().exists(false).build()));

        when(addressServiceClient.validateAddress(contains("Степная"), eq(false)))
                .thenReturn(CompletableFuture.completedFuture(
                        AddressValidationResponse.builder().exists(true).build()));

        // when
        CompletableFuture<ValidationResult> result = registrationService.validate(request);

        // then
        ValidationResult validationResult = result.join();
        assertThat(validationResult.isValid()).isFalse();
        assertThat(validationResult.getMessage()).contains("Адрес отправителя не найден");
    }

    @Test
    void validate_shouldReturnErrorWhenRecipientAddressNotFound() {
        // given
        when(userServiceClient.getUserStatus(userId))
                .thenReturn(CompletableFuture.completedFuture(
                        UserStatusResponse.builder().active(true).build()));

        when(addressServiceClient.validateAddress(contains("Восточная"), eq(false)))
                .thenReturn(CompletableFuture.completedFuture(
                        AddressValidationResponse.builder().exists(true).build()));

        when(addressServiceClient.validateAddress(contains("Степная"), eq(false)))
                .thenReturn(CompletableFuture.completedFuture(
                        AddressValidationResponse.builder().exists(false).build()));

        // when
        CompletableFuture<ValidationResult> result = registrationService.validate(request);

        // then
        ValidationResult validationResult = result.join();
        assertThat(validationResult.isValid()).isFalse();
        assertThat(validationResult.getMessage()).contains("Адрес получателя не найден");
    }

    @Test
    void process_shouldSaveDraftAndEventWhenValidationPasses() {
        // given
        BigDecimal expectedPrice = BigDecimal.valueOf(350.50);

        // Мокаем валидацию (через validate, который вызовется внутри process)
        when(userServiceClient.getUserStatus(userId))
                .thenReturn(CompletableFuture.completedFuture(
                        UserStatusResponse.builder().active(true).build()));

        when(addressServiceClient.validateAddress(anyString(), eq(false)))
                .thenReturn(CompletableFuture.completedFuture(
                        AddressValidationResponse.builder().exists(true).build()));

        // Мокаем рассчет цены
        when(priceCalculationService.calculate(request)).thenReturn(expectedPrice);

        // ВАЖНО: логируем сохранения
        when(draftShipmentRepository.save(any(DraftShipment.class)))
                .thenAnswer(invocation -> {
                    DraftShipment doc = invocation.getArgument(0);
                    System.out.println(">>> SAVING DRAFT: " + doc.getId());
                    return doc;
                });

        when(outboxPaymentEventRepository.save(any(OutboxPaymentEvent.class)))
                .thenAnswer(invocation -> {
                    OutboxPaymentEvent event = invocation.getArgument(0);
                    System.out.println(">>> SAVING EVENT: " + event.getId());
                    return event;
                });

        // Мокаем executor
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            System.out.println(">>> EXECUTOR: executing task");
            task.run();
            return null;
        }).when(mongoExecutor).execute(any(Runnable.class));

        // when
        System.out.println(">>> BEFORE process call");
        CompletableFuture<ProcessingResult> result = registrationService.process(request);
        System.out.println(">>> AFTER process call, before join");

        // then
        ProcessingResult processingResult = result.join();
        System.out.println(">>> AFTER join, result: " + processingResult);
        assertThat(processingResult.getId()).isNotBlank();
        assertThat(processingResult.getMessage()).contains("Ожидает оплаты");

        // Проверяем сохранение черновика
        verify(draftShipmentRepository, times(1)).save(draftShipmentCaptor.capture());
        DraftShipment savedDraft = draftShipmentCaptor.getValue();
        assertThat(savedDraft.getId()).isNotBlank();
        assertThat(savedDraft.getStatus()).isEqualTo(RegistrationService.PENDING);
        assertThat(savedDraft.getUserId()).isEqualTo(userId);
        assertThat(savedDraft.getCreatedAt()).isNotNull();

        // Проверяем сохранение outbox-события
        verify(outboxPaymentEventRepository, times(1)).save(outboxEventCaptor.capture());
        OutboxPaymentEvent savedEvent = outboxEventCaptor.getValue();
        assertThat(savedEvent.getId()).isNotBlank();
        assertThat(savedEvent.getCorrelationId()).isEqualTo(savedDraft.getId());
        assertThat(savedEvent.getType()).isEqualTo(RegistrationService.PAYMENT_REQUEST);

        // Проверяем payload события
        PaymentEvent payload = savedEvent.getPayload();
        assertThat(payload).isNotNull();
        assertThat(payload.getEventId()).isEqualTo(savedEvent.getId());
        assertThat(payload.getCorrelationId()).isEqualTo(savedDraft.getId());
        assertThat(payload.getUserId()).isEqualTo(userId);
        assertThat(payload.getAmount()).isEqualTo(expectedPrice);
    }
}
