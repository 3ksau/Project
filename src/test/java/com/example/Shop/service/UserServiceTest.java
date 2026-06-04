package com.example.Shop.service;

import com.example.Shop.dto.RegisterRequest;
import com.example.Shop.entity.PasswordResetToken;
import com.example.Shop.entity.User;
import com.example.Shop.repository.PasswordResetTokenRepository;
import com.example.Shop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void register_CreatesNewUser() {
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@test.com")
                .password("password123")
                .fullName("New User")
                .phone("+1234567890")
                .build();

        userService.register(request);

        User saved = userRepository.findByEmail("newuser@test.com").orElseThrow();
        assertThat(saved.getEmail()).isEqualTo("newuser@test.com");
        assertThat(saved.getPassword()).isNotEqualTo("password123");
        assertThat(saved.getFullName()).isEqualTo("New User");
        assertThat(saved.getPhone()).isEqualTo("+1234567890");
    }

    @Test
    void register_WhenEmailAlreadyExists_ThrowsException() {
        userService.register(RegisterRequest.builder()
                .email("existing@test.com")
                .password("pass123")
                .build());

        RegisterRequest duplicate = RegisterRequest.builder()
                .email("existing@test.com")
                .password("other456")
                .build();

        assertThrows(RuntimeException.class,
                () -> userService.register(duplicate));
    }

    @Test
    void findByEmail_WhenExists_ReturnsUser() {
        userService.register(RegisterRequest.builder()
                .email("findme@test.com")
                .password("password")
                .build());

        User found = userService.findByEmail("findme@test.com");

        assertThat(found.getEmail()).isEqualTo("findme@test.com");
    }

    @Test
    void findByEmail_WhenNotExists_ThrowsException() {
        assertThrows(UsernameNotFoundException.class,
                () -> userService.findByEmail("nonexistent@test.com"));
    }

    @Test
    void loadUserByUsername_WhenExists_ReturnsUserDetails() {
        userService.register(RegisterRequest.builder()
                .email("loadtest@test.com")
                .password("mypassword")
                .build());

        UserDetails details = userService.loadUserByUsername("loadtest@test.com");

        assertThat(details.getUsername()).isEqualTo("loadtest@test.com");
        assertThat(details.getPassword()).isNotEqualTo("mypassword");
        assertThat(details.getAuthorities()).extracting("authority")
                .contains("ROLE_USER");
    }

    @Test
    void loadUserByUsername_WhenNotExists_ThrowsException() {
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("unknown@test.com"));
    }

    @Test
    void createPasswordResetToken_CreatesTokenForExistingUser() {
        userService.register(RegisterRequest.builder()
                .email("resetuser@test.com")
                .password("pass123")
                .build());

        String token = userService.createPasswordResetToken("resetuser@test.com");

        assertThat(token).isNotBlank();
        PasswordResetToken saved = tokenRepository.findByToken(token).orElseThrow();
        assertThat(saved.getUser().getEmail()).isEqualTo("resetuser@test.com");
        assertThat(saved.isExpired()).isFalse();
    }

    @Test
    void createPasswordResetToken_WhenUserNotFound_ThrowsException() {
        assertThrows(RuntimeException.class,
                () -> userService.createPasswordResetToken("nobody@test.com"));
    }

    @Test
    void resetPassword_WithValidToken_UpdatesPassword() {
        userService.register(RegisterRequest.builder()
                .email("resetme@test.com")
                .password("oldpass")
                .build());

        String token = userService.createPasswordResetToken("resetme@test.com");
        userService.resetPassword(token, "newpass123");

        User user = userRepository.findByEmail("resetme@test.com").orElseThrow();
        assertThat(user.getPassword()).isNotEqualTo("oldpass");
        assertThat(tokenRepository.findByToken(token)).isEmpty();
    }

    @Test
    void resetPassword_WithInvalidToken_ThrowsException() {
        assertThrows(RuntimeException.class,
                () -> userService.resetPassword("invalid-token", "newpass"));
    }

    @Test
    void resetPassword_WithExpiredToken_ThrowsException() {
        userService.register(RegisterRequest.builder()
                .email("expiretest@test.com")
                .password("oldpass")
                .build());

        String token = userService.createPasswordResetToken("expiretest@test.com");
        PasswordResetToken resetToken = tokenRepository.findByToken(token).orElseThrow();
        resetToken.setExpiryDate(java.time.LocalDateTime.now().minusHours(1));
        tokenRepository.save(resetToken);

        assertThrows(RuntimeException.class,
                () -> userService.resetPassword(token, "newpass"));
    }

    @Test
    void validateToken_WithValidToken_DoesNotThrow() {
        userService.register(RegisterRequest.builder()
                .email("valtest@test.com")
                .password("pass")
                .build());

        String token = userService.createPasswordResetToken("valtest@test.com");
        userService.validateToken(token);
    }

    @Test
    void validateToken_WithInvalidToken_ThrowsException() {
        assertThrows(RuntimeException.class,
                () -> userService.validateToken("bad-token"));
    }
}
