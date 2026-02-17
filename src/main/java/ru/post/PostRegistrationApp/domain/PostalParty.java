package ru.post.PostRegistrationApp.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document
public class PostalParty {
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
