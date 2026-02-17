package ru.post.PostRegistrationApp.jpa;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.post.PostRegistrationApp.domain.OutboxPaymentEvent;

@Repository
public interface OutboxPaymentEventRepository extends MongoRepository<OutboxPaymentEvent, String> {
}
