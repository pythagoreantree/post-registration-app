package ru.post.PostRegistrationApp.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.post.PostRegistrationApp.dto.response.UserStatusResponse;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
public class UserServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    @Qualifier("userServiceExecutor")
    private ExecutorService executor;

    @Value("${services.user.url}")
    private String userServiceUrl;

    public CompletableFuture<UserStatusResponse> getUserStatus(UUID userId) {
        log.info("Отправляем запрос в UserService для userId: {}", userId);

        String url = String.format("%s/api/v1/users/%s/status",
                userServiceUrl, userId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                UserStatusResponse response = restTemplate.getForObject(url, UserStatusResponse.class);
                log.info("Получен ответ от UserService: {}", response);
                return response;
            } catch (Exception e) {
                log.error("Ошибка при вызове UserService: {}", e.getMessage());
                return UserStatusResponse.builder()
                        .userId(userId)
                        .exists(false)
                        .active(false)
                        .build();
            }
        }, executor);
    }
}
