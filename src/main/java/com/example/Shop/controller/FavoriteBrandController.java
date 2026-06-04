package com.example.Shop.controller;

import com.example.Shop.service.FavoriteBrandService;
import com.example.Shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/favorite-brands")
@RequiredArgsConstructor
public class FavoriteBrandController {

    private final FavoriteBrandService favoriteBrandService;
    private final UserService userService;

    @PostMapping("/toggle")
    public String toggle(@RequestParam String brand, Authentication auth) {
        if (auth != null) {
            var user = userService.findByEmail(auth.getName());
            favoriteBrandService.toggle(user, brand);
        }
        return "redirect:/catalog?brand=" + brand;
    }
}
