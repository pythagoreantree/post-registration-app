package ru.post.PostRegistrationApp.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostCreatedEvent<T> implements Serializable {

    String id;

    String type;

    LocalDateTime createdAt;

    T payload;
}
