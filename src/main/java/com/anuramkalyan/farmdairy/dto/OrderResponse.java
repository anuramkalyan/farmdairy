package com.anuramkalyan.farmdairy.dto;
import com.anuramkalyan.farmdairy.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
public record OrderResponse(Long orderId, OrderStatus status, BigDecimal total, LocalDateTime createdAt, List<OrderItemResponse> items) {}
