package ru.post.PostRegistrationApp.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PostItemRequest {
    private String type;
    private UUID userId;
    private PostalPartyRequest sender;
    private PostalPartyRequest receiver;
    private String postOfficeCode;
    private String operatorId;
    private String sourceSystem;
    private LocalDateTime acceptedAt;

    @Data
    public static class PostalPartyRequest {
        private String firstName;
        private String lastName;
        private String middleName;
        private String postalCode;
        private String city;
        private String street;
        private String house;
        private String building;
        private String apartment;
    }
}
