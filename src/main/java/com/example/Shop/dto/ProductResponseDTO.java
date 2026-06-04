package com.example.Shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Product information returned by the API")
public class ProductResponseDTO {

    @Schema(description = "Product ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Product name", example = "Kitchen Mixer Chrome")
    private String name;

    @Schema(description = "Product description", example = "High-quality chrome kitchen mixer with swivel spout")
    private String description;

    @Schema(description = "Product price", example = "89.99")
    private BigDecimal price;

    @Schema(description = "Stock quantity", example = "25")
    private Integer stockQuantity;

    @Schema(description = "Image URL", example = "/uploads/image.jpg")
    private String imageUrl;

    @Schema(description = "Brand name", example = "Grohe")
    private String brand;

    @Schema(description = "Article number", example = "ART-12345")
    private String article;

    @Schema(description = "Material", example = "Латунь")
    private String material;

    @Schema(description = "Color / finish", example = "Хром")
    private String color;

    @Schema(description = "Weight (kg)", example = "1.5")
    private java.math.BigDecimal weight;

    @Schema(description = "Warranty", example = "5 лет")
    private String warranty;

    @Schema(description = "Availability", example = "true")
    private Boolean available;

    @Schema(description = "Category ID", example = "1")
    private Long categoryId;

    @Schema(description = "Category name", example = "Kitchen")
    private String categoryName;

    @Schema(description = "Creation timestamp", example = "2026-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-01-20T14:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Average rating (1-5)", example = "4.5")
    private Double averageRating;

    @Schema(description = "Total number of reviews", example = "12")
    private Integer reviewCount;
}
