package com.example.Shop.controller;

import com.example.Shop.dto.ProductResponseDTO;
import com.example.Shop.service.CategoryService;
import com.example.Shop.service.FavoriteBrandService;
import com.example.Shop.service.ProductService;
import com.example.Shop.service.ReviewService;
import com.example.Shop.service.UserService;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final CategoryService categoryService;
    private final ProductService productService;
    private final ReviewService reviewService;
    private final UserService userService;
    private final FavoriteBrandService favoriteBrandService;

    @GetMapping("/")
    public String index(Authentication auth, Model model) {
        model.addAttribute("categories", categoryService.findAll());
        if (auth != null) {
            var user = userService.findByEmail(auth.getName());
            var favoriteBrands = favoriteBrandService.getFavoriteBrands(user);
            if (!favoriteBrands.isEmpty()) {
                model.addAttribute("favoriteBrands", favoriteBrands);
                model.addAttribute("favoriteProducts", productService.findByBrands(favoriteBrands, 8));
            }
        }
        return "index";
    }

    @GetMapping("/catalog")
    public String catalog(@RequestParam(required = false) Long categoryId,
                           @RequestParam(required = false) String q,
                           @RequestParam(required = false) BigDecimal priceMin,
                           @RequestParam(required = false) BigDecimal priceMax,
                           @RequestParam(required = false) String brand,
                           @RequestParam(required = false) Boolean available,
                           @RequestParam(required = false) String material,
                           @RequestParam(required = false) String color,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "12") int size,
                           @RequestParam(defaultValue = "name") String sort,
                           @RequestParam(defaultValue = "asc") String dir,
                           Authentication auth,
                           Model model) {
        Page<ProductResponseDTO> productPage = productService.findFiltered(
                q, categoryId, priceMin, priceMax, brand, available, material, color, page, size, sort, dir);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("query", q);
        model.addAttribute("priceMin", priceMin);
        model.addAttribute("priceMax", priceMax);
        model.addAttribute("selectedBrand", brand);
        model.addAttribute("available", available);
        model.addAttribute("selectedMaterial", material);
        model.addAttribute("selectedColor", color);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("revDir", dir.equals("asc") ? "desc" : "asc");

        if (categoryId != null) {
            model.addAttribute("category", categoryService.findById(categoryId));
        }

        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("brands", productService.findAllBrands());
        model.addAttribute("materials", productService.findAllMaterials());
        model.addAttribute("colors", productService.findAllColors());
        model.addAttribute("selectedCategoryId", categoryId);

        if (auth != null) {
            var user = userService.findByEmail(auth.getName());
            model.addAttribute("favoriteBrands", favoriteBrandService.getFavoriteBrands(user));
        }

        return "catalog";
    }

    @GetMapping("/compare")
    public String compare(@RequestParam(required = false) String ids, Model model) {
        if (ids != null && !ids.isBlank()) {
            var idList = java.util.Arrays.stream(ids.split(","))
                    .filter(s -> !s.isBlank())
                    .map(Long::parseLong)
                    .toList();
            List<ProductResponseDTO> products = productService.findAll().stream()
                    .filter(p -> idList.contains(p.getId()))
                    .toList();
            model.addAttribute("products", products);
        }
        return "compare";
    }

    @GetMapping("/product/{id}")
    public String product(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        model.addAttribute("reviews", reviewService.getReviewsByProductId(id));
        return "product";
    }

    @PostMapping("/product/{id}/review")
    public String addReview(@PathVariable Long id,
                            @RequestParam int rating,
                            @RequestParam(required = false) String comment,
                            @RequestParam(value = "image", required = false) MultipartFile image,
                            Authentication auth) {
        var user = userService.findByEmail(auth.getName());
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            var dir = java.nio.file.Path.of("uploads/reviews");
            try {
                java.nio.file.Files.createDirectories(dir);
                String filename = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                java.nio.file.Path dest = dir.resolve(filename);
                image.transferTo(dest.toFile());
                imageUrl = "/uploads/reviews/" + filename;
            } catch (java.io.IOException e) {
                throw new RuntimeException("Failed to save review image", e);
            }
        }
        reviewService.addReview(id, user, rating, comment, imageUrl);
        return "redirect:/product/" + id;
    }
}
