package com.example.Shop.controller;

import com.example.Shop.dto.ProductRequestDTO;
import com.example.Shop.dto.ProductResponseDTO;
import com.example.Shop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management CRUD API")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/suggestions")
    @Operation(summary = "Search suggestions", description = "Returns product name suggestions for autocomplete")
    public ResponseEntity<List<String>> getSuggestions(
            @RequestParam String q,
            @RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(productService.getSuggestions(q, limit));
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Returns a list of all products in the store")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of products",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<ProductResponseDTO>> getAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/page")
    @Operation(summary = "Get paginated products (for infinite scroll)")
    public ResponseEntity<org.springframework.data.domain.Page<ProductResponseDTO>> getPage(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String color,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String dir) {
        return ResponseEntity.ok(productService.findFiltered(q, categoryId, priceMin, priceMax, brand, available, material, color, page, size, sort, dir));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponseDTO> getById(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody ProductRequestDTO dto) {
        ProductResponseDTO created = productService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponseDTO> update(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO dto) {
        return ResponseEntity.ok(productService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
