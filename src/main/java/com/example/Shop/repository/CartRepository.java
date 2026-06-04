package com.example.Shop.repository;

import com.example.Shop.entity.Cart;
import com.example.Shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);
    Optional<Cart> findByUser(User user);
}
