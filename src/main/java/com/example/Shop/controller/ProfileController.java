package com.example.Shop.controller;

import com.example.Shop.entity.User;
import com.example.Shop.service.OrderService;
import com.example.Shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final OrderService orderService;

    @GetMapping
    public String profile(Authentication auth, Model model) {
        User user = userService.findByEmail(auth.getName());
        model.addAttribute("user", user);
        model.addAttribute("orders", orderService.getUserOrders(user));
        return "profile";
    }

    @PostMapping
    public String updateProfile(Authentication auth,
                                @RequestParam String fullName,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String address,
                                Model model) {
        User user = userService.findByEmail(auth.getName());
        userService.updateProfile(user, fullName, phone, address);
        return "redirect:/profile?updated";
    }

    @GetMapping("/password")
    public String passwordForm() {
        return "change-password";
    }

    @PostMapping("/password")
    public String changePassword(Authentication auth,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String newPasswordConfirm,
                                 Model model) {
        if (!newPassword.equals(newPasswordConfirm)) {
            model.addAttribute("error", "Новые пароли не совпадают");
            return "change-password";
        }
        if (newPassword.length() < 6) {
            model.addAttribute("error", "Пароль должен быть минимум 6 символов");
            return "change-password";
        }
        try {
            User user = userService.findByEmail(auth.getName());
            userService.changePassword(user, currentPassword, newPassword);
            return "redirect:/profile?password";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "change-password";
        }
    }
}
