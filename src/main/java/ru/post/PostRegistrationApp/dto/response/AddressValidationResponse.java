package ru.post.PostRegistrationApp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressValidationResponse {
    private boolean exists;
    private String normalizedAddress;  // "г Москва, ул Тверская, д 7"
    private String postalCode;
    private double confidence;
}
