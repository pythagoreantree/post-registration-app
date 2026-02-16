package ru.post.PostRegistrationApp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.post.PostRegistrationApp.client.AddressServiceClient;
import ru.post.PostRegistrationApp.client.UserServiceClient;
import ru.post.PostRegistrationApp.domain.RegistrationResult;
import ru.post.PostRegistrationApp.dto.response.UserStatusResponse;
import ru.post.PostRegistrationApp.dto.request.PostItemRequest;
import ru.post.PostRegistrationApp.dto.response.AddressValidationResponse;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class RegistrationService {

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private AddressServiceClient addressServiceClient;

    /**
     * Зарегистрировать почтовое отправление
     */
    public RegistrationResult process(PostItemRequest request) {
        log.info("Регистрация отправления: type={}, postOffice={}",
                request.getType(), request.getPostOfficeCode());

        CompletableFuture<UserStatusResponse> userFuture =
                userServiceClient.getUserStatus(request.getUserId());

        userFuture.thenApply(userStatus -> {

            log.info("Получен статус пользователя {}: active={}, exists={}",
                    userStatus.getUserId(),
                    userStatus.isActive(),
                    userStatus.isExists());

            if (!userStatus.isExists()) {
                log.warn("Пользователь {} не найден", request.getUserId());
                return null;
            }

            if (!userStatus.isActive()) {
                log.warn("Пользователь {} не активен", request.getUserId());
                return null;
            }

            log.info("Пользователь {} активен, можем создавать отправление",
                    request.getUserId());
            return null;
        });

        CompletableFuture<AddressValidationResponse> senderAddressFuture =
                addressServiceClient.validateAddress(getFullAddress(request.getSender()), false);

        CompletableFuture<AddressValidationResponse> recipientAddressFuture =
                addressServiceClient.validateAddress(getFullAddress(request.getReceiver()), false);

        CompletableFuture.allOf(userFuture, senderAddressFuture, recipientAddressFuture)
                .thenApply(v -> {
                    UserStatusResponse user = userFuture.join();
                    AddressValidationResponse senderAddr = senderAddressFuture.join();
                    AddressValidationResponse recipientAddr = recipientAddressFuture.join();

                    return RegistrationResult.success("ID");
                });

        log.info("Отправление зарегистрировано с ID: {}", "ID");

        return RegistrationResult.success("ID");
    }

    private String getFullAddress(PostItemRequest.PostalPartyRequest request) {
        StringBuilder sb = new StringBuilder();

        String postalCode = request.getPostalCode();
        if (postalCode != null && !postalCode.isEmpty()) {
            sb.append(postalCode).append(", ");
        }

        sb.append(request.getCity()).append(", ");
        sb.append("ул. ").append(request.getStreet()).append(", ");
        sb.append("д. ").append(request.getHouse());

        String building = request.getBuilding();
        if (building != null && !building.isEmpty()) {
            sb.append(", корп. ").append(building);
        }

        String apartment = request.getApartment();
        if (apartment != null && !apartment.isEmpty()) {
            sb.append(", кв. ").append(apartment);
        }

        return sb.toString();
    }
}
