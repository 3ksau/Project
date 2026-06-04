package com.example.Shop.controller;

import com.example.Shop.service.WishlistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping("/wishlist")
    public String wishlist(Authentication auth, Model model) {
        String email = auth != null ? auth.getName() : null;
        model.addAttribute("items", wishlistService.getUserWishlist(email));
        return "wishlist";
    }

    @PostMapping("/wishlist/toggle/{productId}")
    public String toggle(@PathVariable Long productId, Authentication auth,
                         HttpServletRequest request) {
        boolean added = wishlistService.toggle(auth.getName(), productId);
        String msg = added ? "Добавлено в избранное" : "Удалено из избранного";
        String referer = request.getHeader("Referer");
        String sep = (referer != null && referer.contains("?") ? "&" : "?");
        return "redirect:" + (referer != null ? referer : "/catalog") + sep
                + "toast=" + URLEncoder.encode(msg, StandardCharsets.UTF_8) + "&toastType=success";
    }
}
