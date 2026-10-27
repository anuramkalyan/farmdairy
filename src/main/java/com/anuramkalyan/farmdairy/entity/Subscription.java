package com.anuramkalyan.farmdairy.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "milk_subscriptions")
public class Subscription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriptionFrequency frequency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "next_delivery_date", nullable = false)
    private LocalDate nextDeliveryDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Subscription() {}

    public Subscription(AppUser user, Product product, int quantity, SubscriptionFrequency frequency, LocalDate startDate) {
        this.user = user; this.product = product; this.quantity = quantity;
        this.frequency = frequency; this.status = SubscriptionStatus.ACTIVE;
        this.startDate = startDate; this.nextDeliveryDate = startDate;
        this.createdAt = LocalDateTime.now(); this.updatedAt = LocalDateTime.now();
    }

    public void pause() {
        if (status != SubscriptionStatus.ACTIVE) throw new IllegalStateException("Only active subscriptions can be paused");
        status = SubscriptionStatus.PAUSED; updatedAt = LocalDateTime.now();
    }
    public void resume() {
        if (status != SubscriptionStatus.PAUSED) throw new IllegalStateException("Only paused subscriptions can be resumed");
        status = SubscriptionStatus.ACTIVE;
        if (nextDeliveryDate.isBefore(LocalDate.now())) nextDeliveryDate = LocalDate.now();
        updatedAt = LocalDateTime.now();
    }
    public void cancel() {
        if (status == SubscriptionStatus.CANCELLED) throw new IllegalStateException("Subscription is already cancelled");
        status = SubscriptionStatus.CANCELLED; updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public SubscriptionFrequency getFrequency() { return frequency; }
    public SubscriptionStatus getStatus() { return status; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getNextDeliveryDate() { return nextDeliveryDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
