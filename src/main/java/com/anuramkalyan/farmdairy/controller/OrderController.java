package com.anuramkalyan.farmdairy.controller;

import com.anuramkalyan.farmdairy.dto.OrderResponse;
import com.anuramkalyan.farmdairy.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService service;
    public OrderController(OrderService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(Authentication auth) { return service.placeOrder(auth.getName()); }

    @GetMapping
    public List<OrderResponse> getOrders(Authentication auth) { return service.getOrders(auth.getName()); }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(Authentication auth, @PathVariable Long orderId) {
        return service.getOrder(auth.getName(), orderId);
    }
}
