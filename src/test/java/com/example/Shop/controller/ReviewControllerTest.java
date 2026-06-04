package com.example.Shop.controller;

import com.example.Shop.dto.ReviewRequest;
import com.example.Shop.entity.Category;
import com.example.Shop.entity.Product;
import com.example.Shop.entity.User;
import com.example.Shop.repository.CategoryRepository;
import com.example.Shop.repository.ProductRepository;
import com.example.Shop.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private Product product;
    private User user;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        Category category = categoryRepository.save(
                Category.builder().name("Test").slug("test").build());
        product = productRepository.save(Product.builder()
                .name("Test Product").price(BigDecimal.TEN).stockQuantity(5)
                .available(true).category(category).build());
        user = userRepository.save(User.builder()
                .email("revtest@test.com")
                .password("pass")
                .fullName("Review Tester")
                .role(com.example.Shop.entity.Role.USER)
                .build());
    }

    @Test
    void getReviews_WhenNoReviews_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/products/{id}/reviews", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "revtest@test.com")
    void addReview_WithAuth_ReturnsCreated() throws Exception {
        ReviewRequest request = ReviewRequest.builder()
                .rating(4)
                .comment("Good product")
                .build();

        mockMvc.perform(post("/api/products/{id}/reviews", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.userName").value("Review Tester"));
    }
}
