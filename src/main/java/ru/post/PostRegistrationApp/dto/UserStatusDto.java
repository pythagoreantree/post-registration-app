package ru.post.PostRegistrationApp.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusDto {
    private UUID userId;
    private boolean active;
    private boolean exists;
}
