package com.example.Shop.controller;

import com.example.Shop.service.CartService;
import com.example.Shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    public String viewCart(Authentication auth, Model model) {
        var user = userService.findByEmail(auth.getName());
        var items = cartService.getCartItems(user);
        model.addAttribute("items", items);
        var total = items.stream()
                .map(i -> i.getPrice().multiply(java.math.BigDecimal.valueOf(i.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        model.addAttribute("total", total);
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(Authentication auth,
                            @RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity) {
        var user = userService.findByEmail(auth.getName());
        cartService.addItem(user, productId, quantity);
        return "redirect:/cart?toast=" + URLEncoder.encode("Товар добавлен в корзину", StandardCharsets.UTF_8) + "&toastType=success";
    }

    @PostMapping("/remove/{itemId}")
    public String removeFromCart(Authentication auth, @PathVariable Long itemId) {
        var user = userService.findByEmail(auth.getName());
        cartService.removeItem(user, itemId);
        return "redirect:/cart";
    }

    @PostMapping("/update/{itemId}")
    public String updateQuantity(Authentication auth,
                                 @PathVariable Long itemId,
                                 @RequestParam int quantity) {
        var user = userService.findByEmail(auth.getName());
        cartService.updateItemQuantity(user, itemId, quantity);
        return "redirect:/cart";
    }
}
