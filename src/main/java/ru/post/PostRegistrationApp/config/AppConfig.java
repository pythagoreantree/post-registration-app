package ru.post.PostRegistrationApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean("userServiceExecutor")
    public ExecutorService userServiceExecutor() {
        return Executors.newFixedThreadPool(50);
    }

    @Bean("addressServiceExecutor")
    public ExecutorService addressServiceExecutor() {
        return Executors.newFixedThreadPool(200);
    }
}
