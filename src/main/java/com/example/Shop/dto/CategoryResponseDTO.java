package com.example.Shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Category information returned by the API")
public class CategoryResponseDTO {

    @Schema(description = "Category ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Category name", example = "Kitchen")
    private String name;

    @Schema(description = "URL slug", example = "kitchen")
    private String slug;

    @Schema(description = "Category description", example = "Kitchen faucets, sinks and accessories")
    private String description;

    @Schema(description = "Category image URL", example = "/uploads/categories/image.jpg")
    private String imageUrl;

    @Schema(description = "Creation timestamp", example = "2026-01-15T10:30:00")
    private LocalDateTime createdAt;
}
