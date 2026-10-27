package com.anuramkalyan.farmdairy.repository;

import com.anuramkalyan.farmdairy.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {

    List<CartItem> findAllByUser_Id(Long userId);

    Optional<CartItem> findByIdAndUser_Id(Long itemId, Long userId);

    Optional<CartItem> findByUser_IdAndProduct_Id(
            Long userId,
            Long productId
    );

    boolean existsByProduct_Id(Long productId);
}
