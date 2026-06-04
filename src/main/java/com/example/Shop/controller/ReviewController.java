package com.example.Shop.controller;

import com.example.Shop.dto.ReviewRequest;
import com.example.Shop.dto.ReviewResponse;
import com.example.Shop.entity.Review;
import com.example.Shop.service.ReviewService;
import com.example.Shop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product reviews and ratings API")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all reviews for a product")
    public ResponseEntity<List<ReviewResponse>> getReviews(@PathVariable Long productId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByProductId(productId)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    @Operation(summary = "Add a review to a product")
    public ResponseEntity<ReviewResponse> addReview(@PathVariable Long productId,
                                                     @Valid @RequestBody ReviewRequest request,
                                                     Authentication auth) {
        var user = userService.findByEmail(auth.getName());
        Review review = reviewService.addReview(productId, user, request.getRating(), request.getComment(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(review));
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .userName(review.getUser().getFullName() != null ? review.getUser().getFullName() : review.getUser().getEmail())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
