package ru.post.PostRegistrationApp.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.post.PostRegistrationApp.dto.response.AddressValidationResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
public class AddressServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ExecutorService executor;

    @Value("${services.address.url}")
    private String addressServiceUrl;

    public CompletableFuture<AddressValidationResponse> validateAddress(String address, boolean strict) {
        log.info("Отправляем запрос в AddressService для адреса: {}", address);

        return CompletableFuture.supplyAsync(() -> {
            String url = String.format("%s/api/v1/addresses/validate?address=%s&strict=%s",
                    addressServiceUrl, address, strict);

            try {
                AddressValidationResponse response = restTemplate.getForObject(url, AddressValidationResponse.class);
                log.info("Получен ответ от AddressService: {}", response);
                return response;

            } catch (Exception e) {
                log.error("Ошибка при вызове AddressService: {}", e.getMessage());
                return AddressValidationResponse.builder()
                        .exists(false)
                        .normalizedAddress(address)
                        .confidence(0.0)
                        .build();
            }
        }, executor);
    }
}
