package ru.post.PostRegistrationApp.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusResponse {
    private UUID userId;
    private boolean active;
    private boolean exists;
}
