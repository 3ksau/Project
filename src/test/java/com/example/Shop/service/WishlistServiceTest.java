package com.example.Shop.service;

import com.example.Shop.dto.ProductResponseDTO;
import com.example.Shop.entity.*;
import com.example.Shop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class WishlistServiceTest {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        wishlistRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("wishuser@test.com")
                .password("pass")
                .fullName("Wishlist User")
                .role(Role.USER)
                .build());

        Category category = categoryRepository.save(Category.builder()
                .name("Test")
                .slug("test")
                .build());

        product = productRepository.save(Product.builder()
                .name("Wished Product")
                .price(BigDecimal.valueOf(75))
                .stockQuantity(5)
                .available(true)
                .category(category)
                .build());
    }

    @Test
    void toggle_WhenNotInWishlist_AddsItem() {
        wishlistService.toggle(user.getEmail(), product.getId());

        List<ProductResponseDTO> wishlist = wishlistService.getUserWishlist(user.getEmail());
        assertThat(wishlist).hasSize(1);
        assertThat(wishlist.get(0).getName()).isEqualTo("Wished Product");
    }

    @Test
    void toggle_WhenAlreadyInWishlist_RemovesItem() {
        wishlistService.toggle(user.getEmail(), product.getId());
        assertThat(wishlistService.getUserWishlist(user.getEmail())).hasSize(1);

        wishlistService.toggle(user.getEmail(), product.getId());

        assertThat(wishlistService.getUserWishlist(user.getEmail())).isEmpty();
    }

    @Test
    void isInWishlist_WhenAdded_ReturnsTrue() {
        wishlistService.toggle(user.getEmail(), product.getId());

        boolean inWishlist = wishlistService.isInWishlist(user.getEmail(), product.getId());

        assertThat(inWishlist).isTrue();
    }

    @Test
    void isInWishlist_WhenNotAdded_ReturnsFalse() {
        boolean inWishlist = wishlistService.isInWishlist(user.getEmail(), product.getId());

        assertThat(inWishlist).isFalse();
    }

    @Test
    void getUserWishlist_WhenEmpty_ReturnsEmptyList() {
        List<ProductResponseDTO> wishlist = wishlistService.getUserWishlist(user.getEmail());

        assertThat(wishlist).isEmpty();
    }

    @Test
    void getUserWishlist_ReturnsOnlyUserItems() {
        User otherUser = userRepository.save(User.builder()
                .email("other@test.com")
                .password("pass")
                .role(Role.USER)
                .build());

        wishlistService.toggle(user.getEmail(), product.getId());

        List<ProductResponseDTO> otherWishlist = wishlistService.getUserWishlist(otherUser.getEmail());
        assertThat(otherWishlist).isEmpty();
    }

    @Test
    void wishlistCount_WhenEmailIsNull_ReturnsZero() {
        assertThat(wishlistService.wishlistCount(null)).isZero();
    }

    @Test
    void wishlistCount_ReturnsCorrectCount() {
        assertThat(wishlistService.wishlistCount(user.getEmail())).isZero();

        wishlistService.toggle(user.getEmail(), product.getId());

        assertThat(wishlistService.wishlistCount(user.getEmail())).isEqualTo(1);
    }
}
