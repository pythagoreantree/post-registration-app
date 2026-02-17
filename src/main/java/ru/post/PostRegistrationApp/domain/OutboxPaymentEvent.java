package ru.post.PostRegistrationApp.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@Document(collection = "outbox_payment_events")
public class OutboxPaymentEvent {

    @Field("event_id")
    String id;

    @Field("correlation_id")
    String correlationId;

    @Field("event_type")
    String type;

    @Field("status")
    @Builder.Default
    String status = "PENDING";

    @Field("created_at")
    LocalDateTime createdAt;

    @Field("sent_at")
    LocalDateTime sentAt;
}
