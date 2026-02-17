package ru.post.PostRegistrationApp.jpa;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.post.PostRegistrationApp.domain.DraftShipment;

@Repository
public interface DraftShipmentRepository extends MongoRepository<DraftShipment, String> {
}
