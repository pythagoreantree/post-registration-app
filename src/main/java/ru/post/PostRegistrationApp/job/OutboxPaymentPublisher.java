package ru.post.PostRegistrationApp.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.post.PostRegistrationApp.domain.OutboxPaymentEvent;
import ru.post.PostRegistrationApp.domain.PaymentEvent;
import ru.post.PostRegistrationApp.jpa.OutboxPaymentEventRepository;

import java.util.List;

@Slf4j
@Component
public class OutboxPaymentPublisher {

    public static final String PENDING = "PENDING";
    public static final String SENT = "SENT";

    @Autowired
    private OutboxPaymentEventRepository outboxPaymentEventRepository;

    @Autowired
    private KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @Scheduled(fixedDelay = 5000) // каждые 5 секунд
    public void publishPendingEvents() {
        List<OutboxPaymentEvent> pending = outboxPaymentEventRepository.findByStatus(PENDING);

        for (OutboxPaymentEvent event : pending) {
            try {
                kafkaTemplate.send("payment-requests", event.getCorrelationId(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                event.setStatus(SENT);
                                outboxPaymentEventRepository.save(event);
                                log.info("Событие {} отправлено", event.getId());
                            } else {
                                log.error("Ошибка отправки события {}: {}", event.getId(), ex.getMessage());
                            }
                        });
            } catch (Exception e) {
                log.error("Error publishing event", e);
            }
        }
    }
}
