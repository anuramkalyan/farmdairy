package com.anuramkalyan.farmdairy.config;

import com.anuramkalyan.farmdairy.entity.AppUser;
import com.anuramkalyan.farmdairy.repository.AppUserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.anuramkalyan.farmdairy.entity.Role;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Configuration
public class AdminAccountInitializer {

    @Bean
    ApplicationRunner initializeAdmin(
            AppUserRepository repository,
            PasswordEncoder passwordEncoder,
            Environment environment) {

        return args -> {
            String email = environment.getProperty("ADMIN_EMAIL");
            String password = environment.getProperty("ADMIN_PASSWORD");

            if (email == null && password == null) {
                return;
            }

            if (email == null || email.isBlank()
                    || password == null || password.isBlank()) {
                throw new IllegalStateException(
                        "Set both ADMIN_EMAIL and ADMIN_PASSWORD"
                );
            }

            if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new IllegalStateException(
                        "Admin password must not exceed 72 UTF-8 bytes"
                );
            }

            email = email.trim().toLowerCase(Locale.ROOT);

            if (!repository.existsByEmail(email)) {
                repository.save(new AppUser(
                        "FarmDairy Admin",
                        email,
                        passwordEncoder.encode(password),
                        Role.ADMIN
                ));
            }
        };
    }
}
