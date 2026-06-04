package com.example.Shop.controller;

import com.example.Shop.dto.CategoryRequestDTO;
import com.example.Shop.entity.Category;
import com.example.Shop.repository.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "ADMIN")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
    }

    @Test
    void getAll_ReturnsListOfCategories() throws Exception {
        categoryRepository.save(Category.builder().name("Sinks").slug("sinks").build());
        categoryRepository.save(Category.builder().name("Faucets").slug("faucets").build());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getById_WhenExists_ReturnsCategory() throws Exception {
        Category category = categoryRepository.save(
                Category.builder().name("Sinks").slug("sinks").build());

        mockMvc.perform(get("/api/categories/{id}", category.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sinks"));
    }

    @Test
    void create_ReturnsCreatedCategory() throws Exception {
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("New Category")
                .slug("new-category")
                .build();

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Category"));
    }

    @Test
    void update_ReturnsUpdatedCategory() throws Exception {
        Category category = categoryRepository.save(
                Category.builder().name("Old").slug("old").build());

        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("Updated")
                .slug("updated")
                .build();

        mockMvc.perform(put("/api/categories/{id}", category.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void delete_ReturnsNoContent() throws Exception {
        Category category = categoryRepository.save(
                Category.builder().name("To Delete").slug("to-delete").build());

        mockMvc.perform(delete("/api/categories/{id}", category.getId()))
                .andExpect(status().isNoContent());
    }
}
