package ru.post.PostRegistrationApp.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import ru.post.PostRegistrationApp.dto.request.PostItemRequest;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Document(collection = "draft_shipments")
public class DraftShipment {

    public static final String PENDING = "PENDING";
    @Field("id")
    String id;

    @Field("type")
    private String type;

    @Field("user_id")
    private UUID userId;

    @Field("sender")
    private PostalParty sender;

    @Field("receiver")
    private PostalParty receiver;

    @Field("post_office_code")
    private String postOfficeCode;

    @Field("operator_id")
    private String operatorId;

    @Field("status")
    String status;

    @Field("created_at")
    LocalDateTime createdAt;

    public static DraftShipment fromRequest(PostItemRequest request) {
        return DraftShipment.builder()
                .id(UUID.randomUUID().toString())
                .type(request.getType())
                .userId(request.getUserId())
                .sender(toPostalParty(request.getSender()))
                .receiver(toPostalParty(request.getReceiver()))
                .postOfficeCode(request.getPostOfficeCode())
                .operatorId(request.getOperatorId())
                .status(PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private static PostalParty toPostalParty(PostItemRequest.PostalPartyRequest request) {
        return PostalParty.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .middleName(request.getMiddleName())
                .postalCode(request.getPostalCode())
                .city(request.getCity())
                .street(request.getStreet())
                .house(request.getHouse())
                .building(request.getBuilding())
                .apartment(request.getApartment())
                .build();
    }
}
