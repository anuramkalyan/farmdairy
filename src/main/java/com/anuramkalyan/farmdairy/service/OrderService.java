package com.anuramkalyan.farmdairy.service;

import com.anuramkalyan.farmdairy.dto.*;
import com.anuramkalyan.farmdairy.entity.*;
import com.anuramkalyan.farmdairy.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final CartItemRepository cartItems;
    private final AppUserRepository users;

    public OrderService(OrderRepository orders, CartItemRepository cartItems, AppUserRepository users) {
        this.orders = orders;
        this.cartItems = cartItems;
        this.users = users;
    }

    @Transactional
    public OrderResponse placeOrder(String email) {
        AppUser user = getUser(email);
        List<CartItem> cart = cartItems.findAllByUser_Id(user.getId());
        if (cart.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot place an order with an empty cart");

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart) {
            Product product = item.getProduct();
            if (!product.isAvailable()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is unavailable: " + product.getName());
            if (item.getQuantity() < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart contains an invalid quantity");
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        Order order = new Order(user, total);
        for (CartItem item : cart) {
            Product p = item.getProduct();
            order.addItem(new OrderItem(p.getId(), p.getName(), p.getPrice(), item.getQuantity()));
        }
        Order saved = orders.save(order);
        cartItems.deleteAll(cart);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(String email) {
        AppUser user = getUser(email);
        return orders.findAllByUser_IdOrderByCreatedAtDesc(user.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String email, Long id) {
        AppUser user = getUser(email);
        Order order = orders.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        return toResponse(order);
    }

    private AppUser getUser(String email) {
        return users.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(i.getProductId(), i.getProductName(), i.getUnitPrice(), i.getQuantity(), i.getLineTotal()))
                .toList();
        return new OrderResponse(order.getId(), order.getStatus(), order.getTotal(), order.getCreatedAt(), items);
    }
}
