package com.anuramkalyan.farmdairy.controller;

import com.anuramkalyan.farmdairy.dto.AuthResponse;
import com.anuramkalyan.farmdairy.dto.LoginRequest;
import com.anuramkalyan.farmdairy.dto.RegisterRequest;
import com.anuramkalyan.farmdairy.service.AuthService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public String register(
            @Valid @RequestBody RegisterRequest request) {

        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authService.login(request);
    }
}
