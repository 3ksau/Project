package com.example.Shop.service;

import com.example.Shop.dto.ProductRequestDTO;
import com.example.Shop.dto.ProductResponseDTO;
import com.example.Shop.entity.Category;
import com.example.Shop.entity.Product;
import com.example.Shop.exception.ResourceNotFoundException;
import com.example.Shop.mapper.ProductMapper;
import com.example.Shop.repository.CategoryRepository;
import com.example.Shop.repository.ProductRepository;
import com.example.Shop.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ReviewService reviewService;

    public Page<ProductResponseDTO> findFiltered(String query, Long categoryId,
                                                   BigDecimal priceMin, BigDecimal priceMax,
                                                   String brand, Boolean available,
                                                   String material, String color,
                                                   int page, int size, String sortField, String sortDir) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(
                        ProductSpecification.withFilters(query, categoryId, priceMin, priceMax, brand, available, material, color),
                        pageable)
                .map(productMapper::toResponseDTO)
                .map(this::enrichWithRating);
    }

    @Cacheable(value = "materials", unless = "#result.isEmpty()")
    public List<String> findAllMaterials() {
        return productRepository.findAll()
                .stream()
                .map(Product::getMaterial)
                .filter(m -> m != null && !m.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    @Cacheable(value = "colors", unless = "#result.isEmpty()")
    public List<String> findAllColors() {
        return productRepository.findAll()
                .stream()
                .map(Product::getColor)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    public List<String> getSuggestions(String query, int limit) {
        if (query == null || query.isBlank()) return java.util.List.of();
        return productRepository.findNameSuggestions(query, limit);
    }

    @Cacheable(value = "brands", unless = "#result.isEmpty()")
    public List<String> findAllBrands() {
        return productRepository.findAll()
                .stream()
                .map(Product::getBrand)
                .filter(b -> b != null && !b.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    public List<ProductResponseDTO> findAll() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponseDTO)
                .map(this::enrichWithRating)
                .toList();
    }

    public List<ProductResponseDTO> findByBrands(List<String> brands, int limit) {
        return productRepository.findByBrandIn(brands).stream()
                .map(productMapper::toResponseDTO)
                .map(this::enrichWithRating)
                .limit(limit)
                .toList();
    }

    public List<ProductResponseDTO> findAllByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(productMapper::toResponseDTO)
                .map(this::enrichWithRating)
                .toList();
    }

    public List<ProductResponseDTO> searchByName(String query) {
        return productRepository.findByNameContainingIgnoreCase(query)
                .stream()
                .map(productMapper::toResponseDTO)
                .map(this::enrichWithRating)
                .toList();
    }

    @Cacheable(value = "products", key = "#id")
    public ProductResponseDTO findById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toResponseDTO)
                .map(this::enrichWithRating)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Transactional
    @CacheEvict(value = {"products", "brands", "materials", "colors"}, allEntries = true)
    public ProductResponseDTO create(ProductRequestDTO dto) {
        Product product = productMapper.toEntity(dto);
        setCategory(dto, product);
        product = productRepository.save(product);
        return productMapper.toResponseDTO(product);
    }

    @Transactional
    @CacheEvict(value = {"products", "brands", "materials", "colors"}, allEntries = true)
    public ProductResponseDTO update(Long id, ProductRequestDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        productMapper.updateEntity(dto, product);
        setCategory(dto, product);
        product = productRepository.save(product);
        return productMapper.toResponseDTO(product);
    }

    private ProductResponseDTO enrichWithRating(ProductResponseDTO dto) {
        if (dto != null && dto.getId() != null) {
            dto.setAverageRating(reviewService.getAverageRating(dto.getId()));
            dto.setReviewCount(reviewService.getReviewCount(dto.getId()));
        }
        return dto;
    }

    private void setCategory(ProductRequestDTO dto, Product product) {
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", dto.getCategoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }
    }

    @Transactional
    @CacheEvict(value = {"products", "brands", "materials", "colors"}, allEntries = true)
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }
        productRepository.deleteById(id);
    }
}
