package com.example.Shop.config;

import com.example.Shop.entity.Role;
import com.example.Shop.entity.User;
import com.example.Shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@shop.com")) {
            User admin = User.builder()
                    .email("admin@shop.com")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Administrator")
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Admin created: admin@shop.com / admin123");
        }
    }
}
