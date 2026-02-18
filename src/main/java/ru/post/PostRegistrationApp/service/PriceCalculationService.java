package ru.post.PostRegistrationApp.service;

import org.springframework.stereotype.Service;
import ru.post.PostRegistrationApp.dto.request.PostItemRequest;

import java.math.BigDecimal;

@Service
public class PriceCalculationService {
    public BigDecimal calculate(PostItemRequest request) {
        return BigDecimal.valueOf(100L);
    }
}
