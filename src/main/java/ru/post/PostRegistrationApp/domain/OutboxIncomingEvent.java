package ru.post.PostRegistrationApp.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@Document(collection = "outbox_incoming_events")
public class OutboxIncomingEvent implements Serializable {

    @Field("event_id")
    String id;

    @Field("event_type")
    String type;

    @Field("payload")
    Object payload;

    @Field("status")
    @Builder.Default
    String status = "RECEIVED";

    @Field("created_at")
    LocalDateTime createdAt;

    @Field("received_at")
    LocalDateTime receivedAt;
}
