package ru.post.PostRegistrationApp.jpa;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.post.PostRegistrationApp.domain.OutboxEvent;

@Repository
public interface OutboxMongoRepository extends MongoRepository<OutboxEvent, String> {
}
