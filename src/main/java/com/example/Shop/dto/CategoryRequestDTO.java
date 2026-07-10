package com.example.Shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for creating or updating a category")
public class CategoryRequestDTO {

    @Schema(description = "Category ID (set only for updates)", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(max = 100)
    @Schema(description = "Category name", example = "Kitchen", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 100)
    @Schema(description = "URL slug", example = "kitchen")
    private String slug;

    @Size(max = 500)
    @Schema(description = "Category description", example = "Kitchen faucets, sinks and accessories")
    private String description;

    @Size(max = 500)
    @Schema(description = "Category image URL", example = "/uploads/categories/image.jpg")
    private String imageUrl;
}
