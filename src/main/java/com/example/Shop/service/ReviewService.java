package com.example.Shop.service;

import com.example.Shop.entity.Product;
import com.example.Shop.entity.Review;
import com.example.Shop.entity.User;
import com.example.Shop.exception.ResourceNotFoundException;
import com.example.Shop.repository.ProductRepository;
import com.example.Shop.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public List<Review> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    public Double getAverageRating(Long productId) {
        Double avg = reviewRepository.findAverageRatingByProductId(productId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    public int getReviewCount(Long productId) {
        Integer count = reviewRepository.countByProductId(productId);
        return count != null ? count : 0;
    }

    @Transactional
    public Review addReview(Long productId, User user, Integer rating, String comment, String imageUrl) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(rating)
                .comment(comment)
                .imageUrl(imageUrl)
                .build();
        return reviewRepository.save(review);
    }
}
