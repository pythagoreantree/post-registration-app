package ru.post.PostRegistrationApp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import ru.post.PostRegistrationApp.domain.OutboxEvent;
import ru.post.PostRegistrationApp.domain.RegistrationResult;
import ru.post.PostRegistrationApp.dto.event.PostCreatedEvent;
import ru.post.PostRegistrationApp.dto.request.PostItemRequest;
import ru.post.PostRegistrationApp.jpa.OutboxMongoRepository;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostItemConsumer {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private OutboxMongoRepository outboxRepository;

    @RabbitListener(queues = "post_created.queue")
    public void handlePostCreated(
            PostCreatedEvent<PostItemRequest> event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        log.info("Получено событие: ID={}, Type={}", event.getId(), event.getType());
        log.info("Payload: {}", event.getPayload());

        try {
            outboxRepository.save(OutboxEvent.builder()
                    .id(event.getId())
                    .type(event.getType())
                    .payload(event.getPayload())
                    .createdAt(event.getCreatedAt())
                    .receivedAt(LocalDateTime.now())
                    .build());

            PostItemRequest request = event.getPayload();

            RegistrationResult result = registrationService.process(request);

            channel.basicAck(deliveryTag, false);
            log.info("Событие {} обработано успешно", event.getId());

        } catch (Exception e) {
            log.error("Ошибка обработки события {}: {}", event.getId(), e.getMessage());

            channel.basicNack(deliveryTag, false, false);
        }
    }
}
