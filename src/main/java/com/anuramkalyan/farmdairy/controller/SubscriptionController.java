package com.anuramkalyan.farmdairy.controller;

import com.anuramkalyan.farmdairy.dto.*;
import com.anuramkalyan.farmdairy.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {
    private final SubscriptionService service;
    public SubscriptionController(SubscriptionService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse create(Authentication auth, @Valid @RequestBody CreateSubscriptionRequest request) {
        return service.create(auth.getName(), request);
    }

    @GetMapping
    public List<SubscriptionResponse> getAll(Authentication auth) { return service.getAll(auth.getName()); }

    @GetMapping("/{id}")
    public SubscriptionResponse getOne(Authentication auth, @PathVariable Long id) {
        return service.getOne(auth.getName(), id);
    }

    @PatchMapping("/{id}/pause")
    public SubscriptionResponse pause(Authentication auth, @PathVariable Long id) {
        return service.pause(auth.getName(), id);
    }

    @PatchMapping("/{id}/resume")
    public SubscriptionResponse resume(Authentication auth, @PathVariable Long id) {
        return service.resume(auth.getName(), id);
    }

    @PatchMapping("/{id}/cancel")
    public SubscriptionResponse cancel(Authentication auth, @PathVariable Long id) {
        return service.cancel(auth.getName(), id);
    }
}
