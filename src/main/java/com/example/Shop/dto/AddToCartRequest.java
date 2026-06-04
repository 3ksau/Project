package com.example.Shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to add a product to the shopping cart")
public class AddToCartRequest {

    @NotNull
    @Schema(description = "Product ID to add", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    @Min(1)
    @Schema(description = "Quantity to add", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private int quantity;
}
