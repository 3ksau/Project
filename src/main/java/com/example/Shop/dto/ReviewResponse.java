package com.example.Shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Product review information")
public class ReviewResponse {

    @Schema(description = "Review ID", example = "1")
    private Long id;

    @Schema(description = "Product ID", example = "1")
    private Long productId;

    @Schema(description = "Reviewer display name", example = "John Doe")
    private String userName;

    @Schema(description = "Rating (1-5)", example = "5")
    private Integer rating;

    @Schema(description = "Review text", example = "Great product, highly recommend!")
    private String comment;

    @Schema(description = "Review timestamp", example = "2026-02-10T15:30:00")
    private LocalDateTime createdAt;
}
