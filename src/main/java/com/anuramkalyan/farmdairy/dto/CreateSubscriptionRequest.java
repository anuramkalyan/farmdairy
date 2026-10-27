package com.anuramkalyan.farmdairy.dto;

import com.anuramkalyan.farmdairy.entity.SubscriptionFrequency;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateSubscriptionRequest(
    @NotNull(message = "Product ID is required") Long productId,
    @NotNull(message = "Quantity is required") @Min(value = 1, message = "Quantity must be at least 1") Integer quantity,
    @NotNull(message = "Frequency is required") SubscriptionFrequency frequency,
    @NotNull(message = "Start date is required") LocalDate startDate
) {}
