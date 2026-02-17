package ru.post.PostRegistrationApp.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessingResult {
    private boolean success;
    private String id;
    private String message;

    public static ProcessingResult success(String id) {
        return ProcessingResult.builder()
                .success(true)
                .id(id)
                .message("Processing completed successfully")
                .build();
    }

    public static ProcessingResult error(String message) {
        return ProcessingResult.builder()
                .success(false)
                .message(message)
                .build();
    }
}
