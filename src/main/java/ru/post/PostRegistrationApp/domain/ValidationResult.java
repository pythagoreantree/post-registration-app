package ru.post.PostRegistrationApp.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidationResult {
    private boolean valid;
    private String message;

    public static ValidationResult success() {
        return ValidationResult.builder()
                .valid(true)
                .message("Validation completed successfully")
                .build();
    }

    public static ValidationResult error(String message) {
        return ValidationResult.builder()
                .valid(false)
                .message(message)
                .build();
    }
}

