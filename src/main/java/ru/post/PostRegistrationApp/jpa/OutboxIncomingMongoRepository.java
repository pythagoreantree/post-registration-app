package ru.post.PostRegistrationApp.jpa;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.post.PostRegistrationApp.domain.OutboxIncomingEvent;

@Repository
public interface OutboxIncomingMongoRepository extends MongoRepository<OutboxIncomingEvent, String> {
}
