package com.anuramkalyan.farmdairy.repository;

import com.anuramkalyan.farmdairy.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
    Optional<Subscription> findByIdAndUser_Id(Long id, Long userId);
    boolean existsByProduct_Id(Long productId);
}
