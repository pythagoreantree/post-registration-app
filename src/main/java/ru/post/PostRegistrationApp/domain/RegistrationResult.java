package ru.post.PostRegistrationApp.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationResult {
    private boolean success;
    private String registrationId;
    private String message;

    public static RegistrationResult success(String registrationId) {
        return RegistrationResult.builder()
                .success(true)
                .registrationId(registrationId)
                .message("Registration completed successfully")
                .build();
    }

    public static RegistrationResult error(String message) {
        return RegistrationResult.builder()
                .success(false)
                .message(message)
                .build();
    }
}
