package ru.post.PostRegistrationApp.service;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.post.PostRegistrationApp.client.AddressServiceClient;
import ru.post.PostRegistrationApp.client.UserServiceClient;
import ru.post.PostRegistrationApp.domain.*;
import ru.post.PostRegistrationApp.dto.response.UserStatusResponse;
import ru.post.PostRegistrationApp.dto.request.PostItemRequest;
import ru.post.PostRegistrationApp.dto.response.AddressValidationResponse;
import ru.post.PostRegistrationApp.jpa.DraftShipmentRepository;
import ru.post.PostRegistrationApp.jpa.OutboxPaymentEventRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public class RegistrationService {

    public static final String PENDING = "PENDING";
    public static final String PAYMENT_REQUEST = "PAYMENT_REQUEST";
    public static final String RUB = "RUB";

    @Autowired
    private DraftShipmentRepository draftShipmentRepository;

    @Autowired
    private OutboxPaymentEventRepository outboxPaymentEventRepository;

    @Autowired
    private PriceCalculationService priceCalculationService;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private AddressServiceClient addressServiceClient;

    @Autowired
    @Qualifier("mongoExecutor")
    private ExecutorService mongoExecutor;

    public CompletableFuture<ValidationResult> validate(PostItemRequest request) {
        log.info("Регистрация отправления: type={}, postOffice={}",
                request.getType(), request.getPostOfficeCode());

        CompletableFuture<UserStatusResponse> userFuture =
                userServiceClient.getUserStatus(request.getUserId());

        CompletableFuture<AddressValidationResponse> senderAddressFuture =
                addressServiceClient.validateAddress(getFullAddress(request.getSender()), false);

        CompletableFuture<AddressValidationResponse> recipientAddressFuture =
                addressServiceClient.validateAddress(getFullAddress(request.getReceiver()), false);

        return CompletableFuture.allOf(
                userFuture, senderAddressFuture, recipientAddressFuture)
                .thenApply(v -> {
                    UserStatusResponse user = userFuture.join();
                    AddressValidationResponse senderAddr = senderAddressFuture.join();
                    AddressValidationResponse recipientAddr = recipientAddressFuture.join();

                    if (!user.isActive()) {
                        return ValidationResult.error("Пользователь не активен");
                    }
                    if (!senderAddr.isExists()) {
                        return ValidationResult.error("Адрес отправителя не найден");
                    }
                    if (!recipientAddr.isExists()) {
                        return ValidationResult.error("Адрес получателя не найден");
                    }

                    return ValidationResult.success();
                });
    }

    /**
     * Зарегистрировать почтовое отправление
     */
    public CompletableFuture<ProcessingResult> process(PostItemRequest request) {
        log.info("Регистрация отправления: type={}, postOffice={}",
                request.getType(), request.getPostOfficeCode());

        return validate(request)
                .thenCompose(validation -> {
                    if (!validation.isValid()) {
                        return CompletableFuture.failedFuture(
                                new ValidationException(validation.getMessage())
                        );
                    }
                    DraftShipment draft = DraftShipment.fromRequest(request);
                    draft.setId(UUID.randomUUID().toString());
                    draft.setStatus(PENDING);
                    draft.setCreatedAt(LocalDateTime.now());

                    BigDecimal summ = priceCalculationService.calculate(request);
                    String eventId = UUID.randomUUID().toString();
                    OutboxPaymentEvent event = OutboxPaymentEvent.builder()
                            .id(eventId)
                            .correlationId(draft.getId())
                            .type(PAYMENT_REQUEST)
                            .payload(PaymentEvent.builder()
                                    .eventId(eventId)
                                    .correlationId(draft.getId())
                                    .userId(request.getUserId())
                                    .amount(summ)
                                    .build())
                            .createdAt(LocalDateTime.now())
                            .build();

                    return CompletableFuture.supplyAsync(() -> {
                        try {
                            draftShipmentRepository.save(draft);
                            outboxPaymentEventRepository.save(event);
                            log.info("Черновик {} сохранен, событие {} создано", draft.getId(), event.getId());
                            return ProcessingResult.pending(draft.getId(), "Ожидает оплаты");
                        } catch (Exception e) {
                            log.error("Ошибка сохранения в MongoDB: {}", e.getMessage());
                            throw new RuntimeException("Failed to save draft", e);
                        }
                    }, mongoExecutor);
                });
    }

    private String getFullAddress(PostItemRequest.PostalPartyRequest request) {
        StringBuilder sb = new StringBuilder();

        String postalCode = request.getPostalCode();
        if (postalCode != null && !postalCode.isEmpty()) {
            sb.append(postalCode).append(", ");
        }

        sb.append(request.getCity()).append(", ");
        sb.append("ул. ").append(request.getStreet()).append(", ");
        sb.append("д. ").append(request.getHouse());

        String building = request.getBuilding();
        if (building != null && !building.isEmpty()) {
            sb.append(", корп. ").append(building);
        }

        String apartment = request.getApartment();
        if (apartment != null && !apartment.isEmpty()) {
            sb.append(", кв. ").append(apartment);
        }

        return sb.toString();
    }
}
