package com.example.Shop.service;

import com.example.Shop.entity.*;
import com.example.Shop.exception.ResourceNotFoundException;
import com.example.Shop.repository.OrderItemRepository;
import com.example.Shop.repository.OrderRepository;
import com.example.Shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;

    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    @Transactional
    public Order checkout(User user) {
        List<CartItem> cartItems = cartService.getCartItems(user);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        for (CartItem ci : cartItems) {
            Product product = ci.getProduct();
            if (!product.getAvailable()) {
                throw new RuntimeException("Product '" + product.getName() + "' is not available");
            }
            if (product.getStockQuantity() < ci.getQuantity()) {
                throw new RuntimeException("Insufficient stock for '" + product.getName()
                        + "': requested " + ci.getQuantity() + ", available " + product.getStockQuantity());
            }
        }

        BigDecimal total = cartItems.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.NEW)
                .totalAmount(total)
                .build();
        order = orderRepository.save(order);

        for (CartItem ci : cartItems) {
            Product product = ci.getProduct();
            product.setStockQuantity(product.getStockQuantity() - ci.getQuantity());
            productRepository.save(product);

            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(ci.getQuantity())
                    .price(ci.getPrice())
                    .build();
            orderItemRepository.save(oi);
        }

        cartService.clearCart(user);
        return order;
    }

    @Transactional
    public void updateStatus(Long orderId, OrderStatus status) {
        Order order = getOrderById(orderId);
        OrderStatus oldStatus = order.getStatus();
        if (oldStatus == status) return;
        order.setStatus(status);
        orderRepository.save(order);
        emailService.sendOrderStatusEmail(order, oldStatus);
    }
}
