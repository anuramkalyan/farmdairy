package com.anuramkalyan.farmdairy.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank
        @Size(max = 120)
        String name,

        @Size(max = 1000)
        String description,

        @NotBlank
        @Size(max = 50)
        String category,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal price,

        @NotBlank
        @Size(max = 30)
        String unit,

        Boolean available
) {}


