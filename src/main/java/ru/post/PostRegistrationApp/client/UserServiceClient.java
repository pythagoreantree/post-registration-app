package ru.post.PostRegistrationApp.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.post.PostRegistrationApp.dto.UserStatusDto;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
public class UserServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ExecutorService executor;

    public CompletableFuture<UserStatusDto> getUserStatus(UUID userId) {
        log.info("Отправляем запрос в UserService для userId: {}", userId);

        String url = "http://localhost:8082/api/v1/users/" + userId + "/status";

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Синхронный вызов (но он в другом потоке)
                UserStatusDto response = restTemplate.getForObject(url, UserStatusDto.class);
                log.info("Получен ответ от UserService: {}", response);
                return response;
            } catch (Exception e) {
                // Другие ошибки (таймаут, 500 и т.д.)
                log.error("Ошибка при вызове UserService: {}", e.getMessage());
                return UserStatusDto.builder()
                        .userId(userId)
                        .exists(false)
                        .active(false)
                        .build();
            }
        }, executor);
    }
}
