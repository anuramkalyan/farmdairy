package com.anuramkalyan.farmdairy.service;

import com.anuramkalyan.farmdairy.dto.AddCartItemRequest;
import com.anuramkalyan.farmdairy.dto.CartItemResponse;
import com.anuramkalyan.farmdairy.dto.CartResponse;
import com.anuramkalyan.farmdairy.dto.UpdateCartItemRequest;
import com.anuramkalyan.farmdairy.entity.AppUser;
import com.anuramkalyan.farmdairy.entity.CartItem;
import com.anuramkalyan.farmdairy.entity.Product;
import com.anuramkalyan.farmdairy.repository.AppUserRepository;
import com.anuramkalyan.farmdairy.repository.CartItemRepository;
import com.anuramkalyan.farmdairy.repository.ProductRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final AppUserRepository appUserRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartItemRepository cartItemRepository,
            AppUserRepository appUserRepository,
            ProductRepository productRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.appUserRepository = appUserRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CartItemResponse addItem(
            String email,
            AddCartItemRequest request
    ) {
        AppUser user = getUser(email);

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"
                ));

        if (!product.isAvailable()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product is not available"
            );
        }

        CartItem item = cartItemRepository
                .findByUser_IdAndProduct_Id(user.getId(), product.getId())
                .orElse(null);

        if (item == null) {
            item = new CartItem(user, product, request.quantity());
        } else {
            item.setQuantity(item.getQuantity() + request.quantity());
        }

        CartItem saved = cartItemRepository.save(item);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(String email) {
        AppUser user = getUser(email);

        List<CartItemResponse> items = cartItemRepository
                .findAllByUser_Id(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(items, total);
    }

    @Transactional
    public CartItemResponse updateItem(
            String email,
            Long itemId,
            UpdateCartItemRequest request
    ) {
        AppUser user = getUser(email);

        CartItem item = cartItemRepository
                .findByIdAndUser_Id(itemId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item not found"
                ));

        item.setQuantity(request.quantity());

        return toResponse(cartItemRepository.save(item));
    }

    @Transactional
    public void removeItem(String email, Long itemId) {
        AppUser user = getUser(email);

        CartItem item = cartItemRepository
                .findByIdAndUser_Id(itemId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item not found"
                ));

        cartItemRepository.delete(item);
    }

    private AppUser getUser(String email) {
        return appUserRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));
    }

    private CartItemResponse toResponse(CartItem item) {
        BigDecimal unitPrice = item.getProduct().getPrice();

        BigDecimal lineTotal = unitPrice.multiply(
                BigDecimal.valueOf(item.getQuantity())
        );

        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                unitPrice,
                item.getQuantity(),
                lineTotal
        );
    }
}
