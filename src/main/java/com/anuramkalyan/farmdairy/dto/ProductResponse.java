package com.anuramkalyan.farmdairy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse (
    Long id,
    String name,
    String description,
    String category,
    BigDecimal price,
    String unit,
    boolean available,
    LocalDateTime createdAt
) {}
