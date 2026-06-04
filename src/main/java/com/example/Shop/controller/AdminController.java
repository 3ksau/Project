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

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;

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
                              BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "admin-product-edit";
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
                               BindingResult result) {
        if (result.hasErrors()) {
            return "admin-category-edit";
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
