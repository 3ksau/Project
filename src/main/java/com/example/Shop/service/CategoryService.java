package com.example.Shop.service;

import com.example.Shop.dto.CategoryRequestDTO;
import com.example.Shop.dto.CategoryResponseDTO;
import com.example.Shop.entity.Category;
import com.example.Shop.exception.ResourceNotFoundException;
import com.example.Shop.mapper.CategoryMapper;
import com.example.Shop.repository.CategoryRepository;
import com.example.Shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductRepository productRepository;

    @Cacheable(value = "categories")
    public List<CategoryResponseDTO> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponseDTO)
                .toList();
    }

    public CategoryResponseDTO findById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponseDTO create(CategoryRequestDTO dto) {
        Category category = categoryMapper.toEntity(dto);
        category = categoryRepository.save(category);
        return categoryMapper.toResponseDTO(category);
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponseDTO update(Long id, CategoryRequestDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        categoryMapper.updateEntity(dto, category);
        category = categoryRepository.save(category);
        return categoryMapper.toResponseDTO(category);
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", id);
        }
        if (!productRepository.findByCategoryId(id).isEmpty()) {
            throw new RuntimeException("Cannot delete category with existing products. Remove or reassign products first.");
        }
        categoryRepository.deleteById(id);
    }
}
