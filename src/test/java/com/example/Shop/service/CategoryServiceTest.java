package com.example.Shop.service;

import com.example.Shop.dto.CategoryRequestDTO;
import com.example.Shop.dto.CategoryResponseDTO;
import com.example.Shop.entity.Category;
import com.example.Shop.exception.ResourceNotFoundException;
import com.example.Shop.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category existingCategory;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        existingCategory = categoryRepository.save(Category.builder()
                .name("Test Category")
                .slug("test-category")
                .description("A test category")
                .build());
    }

    @Test
    void findAll_ReturnsAllCategories() {
        categoryRepository.save(Category.builder()
                .name("Another Category")
                .slug("another-category")
                .build());

        List<CategoryResponseDTO> categories = categoryService.findAll();

        assertThat(categories).hasSize(2);
    }

    @Test
    void findById_WhenExists_ReturnsCategory() {
        CategoryResponseDTO result = categoryService.findById(existingCategory.getId());

        assertThat(result.getName()).isEqualTo("Test Category");
        assertThat(result.getSlug()).isEqualTo("test-category");
    }

    @Test
    void findById_WhenNotExists_ThrowsException() {
        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.findById(999L));
    }

    @Test
    void create_PersistsCategory() {
        CategoryRequestDTO dto = CategoryRequestDTO.builder()
                .name("New Category")
                .slug("new-category")
                .description("Brand new")
                .build();

        CategoryResponseDTO result = categoryService.create(dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("New Category");
        assertThat(categoryRepository.count()).isEqualTo(2);
    }

    @Test
    void update_WhenExists_UpdatesCategory() {
        CategoryRequestDTO dto = CategoryRequestDTO.builder()
                .name("Updated Name")
                .slug("updated-slug")
                .description("Updated desc")
                .build();

        CategoryResponseDTO result = categoryService.update(existingCategory.getId(), dto);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getSlug()).isEqualTo("updated-slug");

        Category updated = categoryRepository.findById(existingCategory.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void update_WhenNotExists_ThrowsException() {
        CategoryRequestDTO dto = CategoryRequestDTO.builder()
                .name("Anything")
                .build();

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.update(999L, dto));
    }

    @Test
    void delete_WhenExists_DeletesCategory() {
        categoryService.delete(existingCategory.getId());

        assertThat(categoryRepository.existsById(existingCategory.getId())).isFalse();
    }

    @Test
    void delete_WhenNotExists_ThrowsException() {
        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.delete(999L));
    }
}
