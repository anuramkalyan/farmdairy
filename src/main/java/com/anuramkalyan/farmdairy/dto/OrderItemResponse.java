package com.anuramkalyan.farmdairy.dto;
import java.math.BigDecimal;
public record OrderItemResponse(Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {}
