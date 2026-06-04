package com.example.Shop.service;

import com.example.Shop.dto.RegisterRequest;
import com.example.Shop.entity.PasswordResetToken;
import com.example.Shop.entity.Role;
import com.example.Shop.entity.User;
import com.example.Shop.repository.PasswordResetTokenRepository;
import com.example.Shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository tokenRepository;

    @Value("${app.reset-token-expiration-hours:24}")
    private int expirationHours;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    @Transactional
    public String createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        tokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(expirationHours))
                .build();
        tokenRepository.save(resetToken);
        return token;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(resetToken);
    }

    public void validateToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Reset token has expired");
        }
    }

    @Transactional
    public void updateProfile(User user, String fullName, String phone, String address) {
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAddress(address);
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
