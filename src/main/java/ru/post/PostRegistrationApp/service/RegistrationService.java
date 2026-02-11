package ru.post.PostRegistrationApp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.post.PostRegistrationApp.domain.RegistrationResult;
import ru.post.PostRegistrationApp.dto.request.PostItemRequest;

@Slf4j
@Service
public class RegistrationService {

    /**
     * Зарегистрировать почтовое отправление
     */
    public RegistrationResult process(PostItemRequest request) {
        log.info("Регистрация отправления: type={}, postOffice={}",
                request.getType(), request.getPostOfficeCode());

        // get user id or user info
        // add to dto
        // make request to user service

        // get receiver and sender addressed
        // to dto
        // call to address service

        log.info("Отправление зарегистрировано с ID: {}", "ID");

        return RegistrationResult.success("ID");
    }
}
