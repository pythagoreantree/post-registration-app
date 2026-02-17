package ru.post.PostRegistrationApp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import ru.post.PostRegistrationApp.domain.OutboxIncomingEvent;
import ru.post.PostRegistrationApp.domain.ProcessingResult;
import ru.post.PostRegistrationApp.dto.event.PostCreatedEvent;
import ru.post.PostRegistrationApp.dto.request.PostItemRequest;
import ru.post.PostRegistrationApp.jpa.OutboxIncomingMongoRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostItemConsumer {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private OutboxIncomingMongoRepository outboxRepository;

    @RabbitListener(queues = "post_created.queue")
    public void handlePostCreated(
            PostCreatedEvent<PostItemRequest> event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        log.info("Получено событие: ID={}, Type={}", event.getId(), event.getType());
        log.info("Payload: {}", event.getPayload());

        try {
            outboxRepository.save(OutboxIncomingEvent.builder()
                    .id(event.getId())
                    .type(event.getType())
                    .payload(event.getPayload())
                    .createdAt(event.getCreatedAt())
                    .receivedAt(LocalDateTime.now())
                    .build());

            PostItemRequest request = event.getPayload();

            CompletableFuture<ProcessingResult> future = registrationService.process(request);

            future.whenComplete((result, throwable) -> {
                try {
                    if (throwable != null) {
                        log.error("Ошибка обработки", throwable);
                        channel.basicNack(deliveryTag, false, false);
                        // можем ли мы в dead letter запихнуть?
                    } else {
                        log.info("Обработка успешна, результат: {}", result);
                        channel.basicAck(deliveryTag, false);
                    }
                } catch (IOException e) {
                    log.error("Ошибка при ack/nack", e);
                }
            });

        } catch (Exception e) {
            log.error("Ошибка обработки события {}: {}", event.getId(), e.getMessage());

            channel.basicNack(deliveryTag, false, false);
        }
    }
}
