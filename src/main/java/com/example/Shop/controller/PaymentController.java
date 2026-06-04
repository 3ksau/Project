package com.example.Shop.controller;

import com.example.Shop.entity.Order;
import com.example.Shop.entity.OrderStatus;
import com.example.Shop.service.CartService;
import com.example.Shop.service.OrderService;
import com.example.Shop.service.PaymentService;
import com.example.Shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final UserService userService;

    @GetMapping("/checkout")
    public String checkoutPage(Authentication auth, Model model) {
        var user = userService.findByEmail(auth.getName());
        var items = cartService.getCartItems(user);
        if (items.isEmpty()) return "redirect:/cart";
        var total = items.stream()
                .map(i -> i.getPrice().multiply(java.math.BigDecimal.valueOf(i.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        model.addAttribute("items", items);
        model.addAttribute("total", total);
        return "checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(Authentication auth) {
        var user = userService.findByEmail(auth.getName());
        Order order = orderService.checkout(user);
        String url = paymentService.createPayment(order);
        return "redirect:" + url;
    }

    @GetMapping("/order/{id}/mock-pay")
    public String mockPay(@PathVariable Long id, Authentication auth) {
        var user = userService.findByEmail(auth.getName());
        Order order = orderService.getOrderById(id);
        if (!order.getUser().getId().equals(user.getId())) return "redirect:/orders";
        paymentService.mockConfirmPayment(id);
        return "redirect:/orders";
    }

    @PostMapping("/api/payment/callback")
    public ResponseEntity<String> paymentCallback(@RequestBody Map<String, Object> payload) {
        paymentService.handleCallback(payload);
        return ResponseEntity.ok("OK");
    }
}
