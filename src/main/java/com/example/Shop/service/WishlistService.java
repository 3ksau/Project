package com.example.Shop.service;

import com.example.Shop.dto.ProductResponseDTO;
import com.example.Shop.entity.Product;
import com.example.Shop.entity.User;
import com.example.Shop.entity.WishlistItem;
import com.example.Shop.exception.ResourceNotFoundException;
import com.example.Shop.mapper.ProductMapper;
import com.example.Shop.repository.ProductRepository;
import com.example.Shop.repository.UserRepository;
import com.example.Shop.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;

    public List<ProductResponseDTO> getUserWishlist(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return wishlistRepository.findByUserId(user.getId())
                .stream()
                .map(item -> productMapper.toResponseDTO(item.getProduct()))
                .toList();
    }

    public boolean isInWishlist(String email, Long productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return wishlistRepository.existsByUserIdAndProductId(user.getId(), productId);
    }

    @Transactional
    public boolean toggle(String email, Long productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        var existing = wishlistRepository.findByUserIdAndProductId(user.getId(), productId);
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            return false;
        } else {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
            WishlistItem item = WishlistItem.builder()
                    .user(user)
                    .product(product)
                    .build();
            wishlistRepository.save(item);
            return true;
        }
    }

    public int wishlistCount(String email) {
        if (email == null) return 0;
        return userRepository.findByEmail(email)
                .map(u -> wishlistRepository.findByUserId(u.getId()).size())
                .orElse(0);
    }
}
