package com.example.Shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User registration request")
public class RegisterRequest {

    @NotBlank
    @Email
    @Schema(description = "User email (also used as username)", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "Password (min 6 characters)", example = "securePass123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Size(max = 150)
    @Schema(description = "User full name", example = "John Doe")
    private String fullName;

    @Size(max = 20)
    @Schema(description = "Phone number", example = "+7-999-123-45-67")
    private String phone;
}
