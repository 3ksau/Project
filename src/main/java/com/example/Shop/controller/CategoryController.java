package com.example.Shop.controller;

import com.example.Shop.dto.CategoryRequestDTO;
import com.example.Shop.dto.CategoryResponseDTO;
import com.example.Shop.service.CategoryService;
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

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management CRUD API")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of categories",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryResponseDTO>> getAll() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryResponseDTO> getById(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new category")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<CategoryResponseDTO> create(@Valid @RequestBody CategoryRequestDTO dto) {
        CategoryResponseDTO created = categoryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryResponseDTO> update(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO dto) {
        return ResponseEntity.ok(categoryService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
