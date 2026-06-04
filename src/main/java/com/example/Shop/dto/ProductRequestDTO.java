package com.example.Shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for creating or updating a product")
public class ProductRequestDTO {

    @Schema(description = "Product ID (set only for updates)", example = "null", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 150)
    @Schema(description = "Product name", example = "Kitchen Mixer Chrome", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 2000)
    @Schema(description = "Detailed product description", example = "High-quality chrome kitchen mixer with swivel spout")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Schema(description = "Product price", example = "89.99", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Schema(description = "Quantity in stock", example = "25", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer stockQuantity;

    @Schema(description = "Image URL", example = "/uploads/image.jpg")
    private String imageUrl;

    @Size(max = 100)
    @Schema(description = "Brand name", example = "Grohe")
    private String brand;

    @Size(max = 50)
    @Schema(description = "Product article number", example = "ART-12345")
    private String article;

    @Schema(description = "Material", example = "Латунь")
    private String material;

    @Schema(description = "Color / finish", example = "Хром")
    private String color;

    @Schema(description = "Weight (kg)", example = "1.5")
    private BigDecimal weight;

    @Schema(description = "Warranty", example = "5 лет")
    private String warranty;

    @Schema(description = "Availability status", example = "true")
    private Boolean available;

    @Schema(description = "Category ID", example = "1")
    private Long categoryId;
}
