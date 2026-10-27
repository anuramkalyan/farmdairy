package com.anuramkalyan.farmdairy.service;

import com.anuramkalyan.farmdairy.dto.*;
import com.anuramkalyan.farmdairy.entity.*;
import com.anuramkalyan.farmdairy.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptions;
    private final AppUserRepository users;
    private final ProductRepository products;

    public SubscriptionService(SubscriptionRepository subscriptions, AppUserRepository users, ProductRepository products) {
        this.subscriptions = subscriptions; this.users = users; this.products = products;
    }

    @Transactional
    public SubscriptionResponse create(String email, CreateSubscriptionRequest request) {
        AppUser user = getUser(email);
        Product product = products.findById(request.productId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        if (!product.isAvailable()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is not available for subscription");
        if (request.startDate().isBefore(LocalDate.now())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be in the past");
        Subscription subscription = new Subscription(user, product, request.quantity(), request.frequency(), request.startDate());
        return toResponse(subscriptions.save(subscription));
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getAll(String email) {
        AppUser user = getUser(email);
        return subscriptions.findAllByUser_IdOrderByCreatedAtDesc(user.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getOne(String email, Long id) {
        return toResponse(getOwned(email, id));
    }

    @Transactional
    public SubscriptionResponse pause(String email, Long id) {
        Subscription s = getOwned(email, id);
        changeStatus(s, 0);
        return toResponse(s);
    }

    @Transactional
    public SubscriptionResponse resume(String email, Long id) {
        Subscription s = getOwned(email, id);
        changeStatus(s, 1);
        return toResponse(s);
    }

    @Transactional
    public SubscriptionResponse cancel(String email, Long id) {
        Subscription s = getOwned(email, id);
        changeStatus(s, 2);
        return toResponse(s);
    }

    private void changeStatus(Subscription s, int action) {
        try {
            if (action == 0) s.pause();
            else if (action == 1) s.resume();
            else s.cancel();
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }
    }

    private Subscription getOwned(String email, Long id) {
        AppUser user = getUser(email);
        return subscriptions.findByIdAndUser_Id(id, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found"));
    }

    private AppUser getUser(String email) {
        return users.findByEmail(email.trim().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private SubscriptionResponse toResponse(Subscription s) {
        return new SubscriptionResponse(s.getId(), s.getProduct().getId(), s.getProduct().getName(),
            s.getQuantity(), s.getFrequency(), s.getStatus(), s.getStartDate(),
            s.getNextDeliveryDate(), s.getCreatedAt());
    }
}
