package com.example.Shop.controller;

import com.example.Shop.dto.CategoryRequestDTO;
import com.example.Shop.dto.CategoryResponseDTO;
import com.example.Shop.dto.ProductRequestDTO;
import com.example.Shop.dto.ProductResponseDTO;
import com.example.Shop.service.CategoryService;
import com.example.Shop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final ProductService productService;
    private final CategoryService categoryService;

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @GetMapping
    public String index() {
        return "redirect:/admin/orders";
    }

    // --- Products ---
    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", productService.findAll());
        return "admin-products";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new ProductRequestDTO());
        model.addAttribute("categories", categoryService.findAll());
        return "admin-product-edit";
    }

    @GetMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model) {
        ProductResponseDTO product = productService.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll());
        return "admin-product-edit";
    }

    @PostMapping("/products/save")
    public String saveProduct(@Valid @ModelAttribute("product") ProductRequestDTO dto,
                              BindingResult result,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "admin-product-edit";
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                var baseDir = Paths.get(System.getProperty("user.dir"), uploadPath, "products");
                java.nio.file.Files.createDirectories(baseDir);
                String filename = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                var dest = baseDir.resolve(filename);
                imageFile.transferTo(dest.toFile());
                dto.setImageUrl("/" + uploadPath + "/products/" + filename);
                log.info("Saved product image to: {}", dest);
            } catch (java.io.IOException e) {
                log.error("Failed to save product image", e);
                throw new RuntimeException("Failed to save product image: " + e.getMessage(), e);
            }
        }
        if (dto.getCategoryId() != null && dto.getCategoryId() == 0) {
            dto.setCategoryId(null);
        }
        if (dto.getId() != null) {
            productService.update(dto.getId(), dto);
        } else {
            productService.create(dto);
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return "redirect:/admin/products";
    }

    // --- Categories ---
    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin-categories";
    }

    @GetMapping("/categories/new")
    public String newCategory(Model model) {
        model.addAttribute("category", new CategoryRequestDTO());
        return "admin-category-edit";
    }

    @GetMapping("/categories/{id}/edit")
    public String editCategory(@PathVariable Long id, Model model) {
        CategoryResponseDTO category = categoryService.findById(id);
        model.addAttribute("category", category);
        return "admin-category-edit";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@Valid @ModelAttribute("category") CategoryRequestDTO dto,
                               BindingResult result,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        if (result.hasErrors()) {
            return "admin-category-edit";
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                var baseDir = java.nio.file.Paths.get(System.getProperty("user.dir"), uploadPath, "categories");
                java.nio.file.Files.createDirectories(baseDir);
                String filename = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                var dest = baseDir.resolve(filename);
                imageFile.transferTo(dest.toFile());
                dto.setImageUrl("/" + uploadPath + "/categories/" + filename);
                log.info("Saved category image: {}", dest);
            } catch (java.io.IOException e) {
                log.error("Failed to save category image", e);
                throw new RuntimeException("Failed to save category image", e);
            }
        }
        if (dto.getId() != null) {
            categoryService.update(dto.getId(), dto);
        } else {
            categoryService.create(dto);
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return "redirect:/admin/categories";
    }
}
