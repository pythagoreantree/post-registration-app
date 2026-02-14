package ru.post.PostRegistrationApp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.post.PostRegistrationApp.client.UserServiceClient;
import ru.post.PostRegistrationApp.domain.RegistrationResult;
import ru.post.PostRegistrationApp.dto.UserStatusDto;
import ru.post.PostRegistrationApp.dto.request.PostItemRequest;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class RegistrationService {

    @Autowired
    private UserServiceClient userServiceClient;

    /**
     * Зарегистрировать почтовое отправление
     */
    public RegistrationResult process(PostItemRequest request) {
        log.info("Регистрация отправления: type={}, postOffice={}",
                request.getType(), request.getPostOfficeCode());

        CompletableFuture<UserStatusDto> userFuture =
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

        // get receiver and sender addressed
        // to dto
        // call to address service

        log.info("Отправление зарегистрировано с ID: {}", "ID");

        return RegistrationResult.success("ID");
    }
}
