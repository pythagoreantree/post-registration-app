package ru.post.PostRegistrationApp.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class PaymentEvent {

    private String eventId;
    private String correlationId;
    private UUID userId;
    private BigDecimal amount;
}
