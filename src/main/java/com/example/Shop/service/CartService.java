package com.example.Shop.service;

import com.example.Shop.entity.*;
import com.example.Shop.exception.ResourceNotFoundException;
import com.example.Shop.repository.CartItemRepository;
import com.example.Shop.repository.CartRepository;
import com.example.Shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    public List<CartItem> getCartItems(User user) {
        return cartRepository.findByUser(user)
                .map(cart -> cartItemRepository.findByCart(cart))
                .orElse(List.of());
    }

    @Transactional
    public void addItem(User user, Long productId, int quantity) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        if (!product.getAvailable()) {
            throw new RuntimeException("Product '" + product.getName() + "' is not available");
        }

        var existing = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
        int newQty = existing.map(i -> i.getQuantity() + quantity).orElse(quantity);

        if (newQty > product.getStockQuantity()) {
            throw new RuntimeException("Insufficient stock for '" + product.getName()
                    + "': requested " + newQty + ", available " + product.getStockQuantity());
        }

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .price(product.getPrice())
                    .build();
            cartItemRepository.save(item);
        }
    }

    @Transactional
    public void updateItemQuantity(User user, Long itemId, int quantity) {
        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Item does not belong to your cart");
        }
        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
    }

    @Transactional
    public void removeItem(User user, Long itemId) {
        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Item does not belong to your cart");
        }
        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteByCart(cart);
    }
}
