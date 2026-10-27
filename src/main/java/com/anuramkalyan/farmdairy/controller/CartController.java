package com.anuramkalyan.farmdairy.controller;

import com.anuramkalyan.farmdairy.dto.AddCartItemRequest;
import com.anuramkalyan.farmdairy.dto.CartItemResponse;
import com.anuramkalyan.farmdairy.dto.CartResponse;
import com.anuramkalyan.farmdairy.dto.UpdateCartItemRequest;
import com.anuramkalyan.farmdairy.service.CartService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItemResponse addItem(
            Authentication authentication,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        return cartService.addItem(authentication.getName(), request);
    }

    @GetMapping
    public CartResponse getCart(Authentication authentication) {
        return cartService.getCart(authentication.getName());
    }

    @PatchMapping("/items/{itemId}")
    public CartItemResponse updateItem(
            Authentication authentication,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return cartService.updateItem(
                authentication.getName(), itemId, request
        );
    }

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(
            Authentication authentication,
            @PathVariable Long itemId
    ) {
        cartService.removeItem(authentication.getName(), itemId);
    }
}
