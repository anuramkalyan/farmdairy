package com.anuramkalyan.farmdairy.dto;

import com.anuramkalyan.farmdairy.entity.SubscriptionFrequency;
import com.anuramkalyan.farmdairy.entity.SubscriptionStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SubscriptionResponse(
    Long subscriptionId, Long productId, String productName, int quantity,
    SubscriptionFrequency frequency, SubscriptionStatus status,
    LocalDate startDate, LocalDate nextDeliveryDate, LocalDateTime createdAt
) {}
