package ru.post.PostRegistrationApp.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostCreatedEvent implements Serializable {

    String id;

    String type;

    LocalDateTime createdAt;

    Object payload;
}
