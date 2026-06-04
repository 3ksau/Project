package com.example.Shop.controller;

import com.example.Shop.dto.ProductRequestDTO;
import com.example.Shop.entity.Category;
import com.example.Shop.entity.Product;
import com.example.Shop.repository.CategoryRepository;
import com.example.Shop.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "ADMIN")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        category = categoryRepository.save(Category.builder()
                .name("Test Category")
                .slug("test-category")
                .build());
    }

    @Test
    void getAll_ReturnsListOfProducts() throws Exception {
        productRepository.save(Product.builder()
                .name("Product A").price(BigDecimal.TEN).stockQuantity(5)
                .available(true).category(category).build());
        productRepository.save(Product.builder()
                .name("Product B").price(BigDecimal.valueOf(20)).stockQuantity(3)
                .available(true).category(category).build());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getById_WhenExists_ReturnsProduct() throws Exception {
        Product product = productRepository.save(Product.builder()
                .name("Product A").price(BigDecimal.TEN).stockQuantity(5)
                .available(true).category(category).build());

        mockMvc.perform(get("/api/products/{id}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Product A"));
    }

    @Test
    void create_ReturnsCreatedProduct() throws Exception {
        ProductRequestDTO request = ProductRequestDTO.builder()
                .name("New Product")
                .price(BigDecimal.valueOf(50))
                .stockQuantity(10)
                .available(true)
                .categoryId(category.getId())
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Product"));
    }

    @Test
    void update_ReturnsUpdatedProduct() throws Exception {
        Product product = productRepository.save(Product.builder()
                .name("Old Name").price(BigDecimal.TEN).stockQuantity(5)
                .available(true).category(category).build());

        ProductRequestDTO request = ProductRequestDTO.builder()
                .name("Updated Name")
                .price(BigDecimal.valueOf(100))
                .stockQuantity(10)
                .available(true)
                .categoryId(category.getId())
                .build();

        mockMvc.perform(put("/api/products/{id}", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void delete_ReturnsNoContent() throws Exception {
        Product product = productRepository.save(Product.builder()
                .name("To Delete").price(BigDecimal.TEN).stockQuantity(1)
                .available(true).category(category).build());

        mockMvc.perform(delete("/api/products/{id}", product.getId()))
                .andExpect(status().isNoContent());
    }
}
