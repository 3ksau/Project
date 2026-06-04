package com.example.Shop.service;

import com.example.Shop.entity.*;
import com.example.Shop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("reviewuser@test.com")
                .password("pass")
                .fullName("Review User")
                .role(Role.USER)
                .build());

        Category category = categoryRepository.save(Category.builder()
                .name("Test")
                .slug("test")
                .build());

        product = productRepository.save(Product.builder()
                .name("Reviewable Product")
                .price(BigDecimal.valueOf(100))
                .stockQuantity(10)
                .available(true)
                .category(category)
                .build());
    }

    @Test
    void addReview_CreatesReview() {
        Review review = reviewService.addReview(product.getId(), user, 5, "Excellent!", null);

        assertThat(review.getId()).isNotNull();
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getComment()).isEqualTo("Excellent!");
        assertThat(review.getProduct().getId()).isEqualTo(product.getId());
        assertThat(review.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void addReview_WhenRatingOutOfRange_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> reviewService.addReview(product.getId(), user, 6, "Too high", null));
        assertThrows(IllegalArgumentException.class,
                () -> reviewService.addReview(product.getId(), user, 0, "Too low", null));
    }

    @Test
    void addReview_WhenProductNotFound_ThrowsException() {
        assertThrows(com.example.Shop.exception.ResourceNotFoundException.class,
                () -> reviewService.addReview(999L, user, 3, "No product", null));
    }

    @Test
    void getReviewsByProductId_ReturnsReviewsOrderedByDate() {
        reviewService.addReview(product.getId(), user, 4, "Good", null);
        reviewService.addReview(product.getId(), user, 5, "Amazing", null);

        List<Review> reviews = reviewService.getReviewsByProductId(product.getId());

        assertThat(reviews).hasSize(2);
    }

    @Test
    void getReviewsByProductId_WhenNoReviews_ReturnsEmpty() {
        List<Review> reviews = reviewService.getReviewsByProductId(product.getId());

        assertThat(reviews).isEmpty();
    }

    @Test
    void getAverageRating_CalculatesCorrectly() {
        reviewService.addReview(product.getId(), user, 4, "Good", null);
        reviewService.addReview(product.getId(), user, 5, "Great", null);

        Double avg = reviewService.getAverageRating(product.getId());

        assertThat(avg).isEqualTo(4.5);
    }

    @Test
    void getAverageRating_WhenNoReviews_ReturnsZero() {
        Double avg = reviewService.getAverageRating(product.getId());

        assertThat(avg).isZero();
    }

    @Test
    void getReviewCount_ReturnsCorrectCount() {
        reviewService.addReview(product.getId(), user, 3, "OK", null);

        int count = reviewService.getReviewCount(product.getId());

        assertThat(count).isEqualTo(1);
    }

    @Test
    void getReviewCount_WhenNoReviews_ReturnsZero() {
        int count = reviewService.getReviewCount(product.getId());

        assertThat(count).isZero();
    }
}
