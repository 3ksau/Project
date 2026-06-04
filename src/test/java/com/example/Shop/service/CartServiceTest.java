package com.example.Shop.service;

import com.example.Shop.entity.*;
import com.example.Shop.exception.ResourceNotFoundException;
import com.example.Shop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("cartuser@test.com")
                .password("pass")
                .fullName("Cart User")
                .role(Role.USER)
                .build());

        Category category = categoryRepository.save(Category.builder()
                .name("Test")
                .slug("test")
                .build());

        product = productRepository.save(Product.builder()
                .name("Test Product")
                .price(BigDecimal.valueOf(50))
                .stockQuantity(10)
                .available(true)
                .category(category)
                .build());
    }

    @Test
    void getOrCreateCart_WhenNoCartExists_CreatesNewCart() {
        Cart cart = cartService.getOrCreateCart(user);

        assertThat(cart.getId()).isNotNull();
        assertThat(cart.getUser().getId()).isEqualTo(user.getId());
        assertThat(cartRepository.count()).isEqualTo(1);
    }

    @Test
    void getOrCreateCart_WhenCartExists_ReturnsExistingCart() {
        Cart first = cartService.getOrCreateCart(user);
        Cart second = cartService.getOrCreateCart(user);

        assertThat(first.getId()).isEqualTo(second.getId());
        assertThat(cartRepository.count()).isEqualTo(1);
    }

    @Test
    void addItem_AddsItemToCart() {
        cartService.addItem(user, product.getId(), 2);

        List<CartItem> items = cartService.getCartItems(user);
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getQuantity()).isEqualTo(2);
        assertThat(items.get(0).getProduct().getId()).isEqualTo(product.getId());
        assertThat(items.get(0).getPrice()).isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    void addItem_WhenProductNotFound_ThrowsException() {
        assertThrows(ResourceNotFoundException.class,
                () -> cartService.addItem(user, 999L, 1));
    }

    @Test
    void removeItem_RemovesItemFromCart() {
        cartService.addItem(user, product.getId(), 1);
        List<CartItem> items = cartService.getCartItems(user);
        Long itemId = items.get(0).getId();

        cartService.removeItem(user, itemId);

        assertThat(cartService.getCartItems(user)).isEmpty();
    }

    @Test
    void removeItem_WhenItemNotInUsersCart_ThrowsException() {
        User otherUser = userRepository.save(User.builder()
                .email("other@test.com")
                .password("pass")
                .role(Role.USER)
                .build());
        cartService.addItem(user, product.getId(), 1);
        Long itemId = cartService.getCartItems(user).get(0).getId();

        assertThrows(RuntimeException.class,
                () -> cartService.removeItem(otherUser, itemId));
    }

    @Test
    void clearCart_RemovesAllItems() {
        cartService.addItem(user, product.getId(), 2);
        assertThat(cartService.getCartItems(user)).isNotEmpty();

        cartService.clearCart(user);

        assertThat(cartService.getCartItems(user)).isEmpty();
    }

    @Test
    void getCartItems_WhenNoCart_ReturnsEmpty() {
        List<CartItem> items = cartService.getCartItems(user);

        assertThat(items).isEmpty();
    }
}
