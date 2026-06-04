package com.example.Shop.service;

import com.example.Shop.dto.ProductRequestDTO;
import com.example.Shop.dto.ProductResponseDTO;
import com.example.Shop.entity.Category;
import com.example.Shop.entity.Product;
import com.example.Shop.exception.ResourceNotFoundException;
import com.example.Shop.repository.CategoryRepository;
import com.example.Shop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        category = categoryRepository.save(Category.builder()
                .name("Kitchen")
                .slug("kitchen")
                .build());
        product = productRepository.save(Product.builder()
                .name("Kitchen Mixer")
                .price(BigDecimal.valueOf(100))
                .stockQuantity(10)
                .available(true)
                .brand("BrandA")
                .article("ART-001")
                .category(category)
                .build());
    }

    @Test
    void findAll_ReturnsAllProducts() {
        productRepository.save(Product.builder()
                .name("Bath Tap")
                .price(BigDecimal.valueOf(50))
                .stockQuantity(5)
                .available(true)
                .category(category)
                .build());

        List<ProductResponseDTO> products = productService.findAll();

        assertThat(products).hasSize(2);
    }

    @Test
    void findById_WhenExists_ReturnsProduct() {
        ProductResponseDTO result = productService.findById(product.getId());

        assertThat(result.getName()).isEqualTo("Kitchen Mixer");
        assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(result.getCategoryId()).isEqualTo(category.getId());
        assertThat(result.getCategoryName()).isEqualTo("Kitchen");
    }

    @Test
    void findById_WhenNotExists_ThrowsException() {
        assertThrows(ResourceNotFoundException.class,
                () -> productService.findById(999L));
    }

    @Test
    void create_PersistsProduct() {
        ProductRequestDTO dto = ProductRequestDTO.builder()
                .name("New Product")
                .price(BigDecimal.valueOf(200))
                .stockQuantity(15)
                .available(true)
                .brand("BrandB")
                .categoryId(category.getId())
                .build();

        ProductResponseDTO result = productService.create(dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("New Product");
        assertThat(result.getCategoryId()).isEqualTo(category.getId());
        assertThat(productRepository.count()).isEqualTo(2);
    }

    @Test
    void create_WhenCategoryNotFound_ThrowsException() {
        ProductRequestDTO dto = ProductRequestDTO.builder()
                .name("Orphan Product")
                .price(BigDecimal.valueOf(50))
                .stockQuantity(1)
                .categoryId(999L)
                .build();

        assertThrows(ResourceNotFoundException.class,
                () -> productService.create(dto));
    }

    @Test
    void update_WhenExists_UpdatesProduct() {
        ProductRequestDTO dto = ProductRequestDTO.builder()
                .name("Updated Mixer")
                .price(BigDecimal.valueOf(150))
                .stockQuantity(20)
                .available(false)
                .brand("BrandB")
                .build();

        ProductResponseDTO result = productService.update(product.getId(), dto);

        assertThat(result.getName()).isEqualTo("Updated Mixer");
        assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(150));

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Mixer");
    }

    @Test
    void update_WhenNotExists_ThrowsException() {
        ProductRequestDTO dto = ProductRequestDTO.builder()
                .name("Ghost")
                .price(BigDecimal.valueOf(10))
                .stockQuantity(1)
                .build();

        assertThrows(ResourceNotFoundException.class,
                () -> productService.update(999L, dto));
    }

    @Test
    void delete_WhenExists_DeletesProduct() {
        productService.delete(product.getId());

        assertThat(productRepository.existsById(product.getId())).isFalse();
    }

    @Test
    void delete_WhenNotExists_ThrowsException() {
        assertThrows(ResourceNotFoundException.class,
                () -> productService.delete(999L));
    }

    @Test
    void searchByName_FindsMatchingProducts() {
        productRepository.save(Product.builder()
                .name("Kitchen Sink")
                .price(BigDecimal.valueOf(80))
                .stockQuantity(3)
                .available(true)
                .category(category)
                .build());

        List<ProductResponseDTO> results = productService.searchByName("kitchen");

        assertThat(results).hasSize(2);
        assertThat(results).extracting(ProductResponseDTO::getName)
                .contains("Kitchen Mixer", "Kitchen Sink");
    }

    @Test
    void searchByName_WhenNoMatch_ReturnsEmpty() {
        List<ProductResponseDTO> results = productService.searchByName("nonexistent");

        assertThat(results).isEmpty();
    }

    @Test
    void findAllByCategoryId_ReturnsProductsInCategory() {
        Category other = categoryRepository.save(Category.builder()
                .name("Bathroom")
                .slug("bathroom")
                .build());
        productRepository.save(Product.builder()
                .name("Bath Tap")
                .price(BigDecimal.valueOf(50))
                .stockQuantity(5)
                .available(true)
                .category(other)
                .build());

        List<ProductResponseDTO> results = productService.findAllByCategoryId(category.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Kitchen Mixer");
    }

    @Test
    void findAllBrands_ReturnsDistinctSortedBrands() {
        productRepository.save(Product.builder()
                .name("Product B")
                .price(BigDecimal.valueOf(30))
                .stockQuantity(1)
                .available(true)
                .brand("BrandB")
                .category(category)
                .build());
        productRepository.save(Product.builder()
                .name("Product C")
                .price(BigDecimal.valueOf(40))
                .stockQuantity(1)
                .available(true)
                .brand("BrandA")
                .category(category)
                .build());

        List<String> brands = productService.findAllBrands();

        assertThat(brands).containsExactly("BrandA", "BrandB");
    }

    @Test
    void findFiltered_WithPagination_ReturnsPage() {
        for (int i = 0; i < 5; i++) {
            productRepository.save(Product.builder()
                    .name("Product " + i)
                    .price(BigDecimal.valueOf(10 * i))
                    .stockQuantity(1)
                    .available(true)
                    .category(category)
                    .build());
        }

        Page<ProductResponseDTO> page = productService.findFiltered(
                null, null, null, null, null, null, null, null,
                0, 3, "name", "asc");

        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(6);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void findFiltered_WithCategoryFilter_ReturnsFilteredProducts() {
        Category other = categoryRepository.save(Category.builder()
                .name("Bathroom")
                .slug("bathroom")
                .build());
        productRepository.save(Product.builder()
                .name("Bath Tap")
                .price(BigDecimal.valueOf(50))
                .stockQuantity(5)
                .available(true)
                .category(other)
                .build());

        Page<ProductResponseDTO> page = productService.findFiltered(
                null, other.getId(), null, null, null, null, null, null,
                0, 10, "name", "asc");

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Bath Tap");
    }

    @Test
    void findFiltered_WithPriceRange_ReturnsFilteredProducts() {
        productRepository.save(Product.builder()
                .name("Cheap Item")
                .price(BigDecimal.valueOf(10))
                .stockQuantity(1)
                .available(true)
                .category(category)
                .build());
        productRepository.save(Product.builder()
                .name("Expensive Item")
                .price(BigDecimal.valueOf(500))
                .stockQuantity(1)
                .available(true)
                .category(category)
                .build());

        Page<ProductResponseDTO> page = productService.findFiltered(
                null, null, BigDecimal.valueOf(50), BigDecimal.valueOf(150),
                null, null, null, null, 0, 10, "name", "asc");

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Kitchen Mixer");
    }

    @Test
    void findFiltered_WithBrandFilter_ReturnsFilteredProducts() {
        Page<ProductResponseDTO> page = productService.findFiltered(
                null, null, null, null, "BrandA", null, null, null,
                0, 10, "name", "asc");

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getBrand()).isEqualTo("BrandA");
    }
}
