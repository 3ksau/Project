package com.example.Shop.repository;

import com.example.Shop.entity.Cart;
import com.example.Shop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart(Cart cart);
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    void deleteByCart(Cart cart);
}
