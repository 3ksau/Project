package com.example.Shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to add a product review")
public class ReviewRequest {

    @NotNull
    @Min(1) @Max(5)
    @Schema(description = "Rating from 1 to 5", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer rating;

    @Size(max = 2000)
    @Schema(description = "Review comment", example = "Great product, highly recommend!")
    private String comment;
}
