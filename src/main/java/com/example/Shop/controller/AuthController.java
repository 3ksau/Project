package com.example.Shop.controller;

import com.example.Shop.dto.RegisterRequest;
import com.example.Shop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid RegisterRequest request, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("error", "Проверьте правильность заполнения полей");
            return "register";
        }
        try {
            userService.register(request);
            return "redirect:/login?toast=" + URLEncoder.encode("Регистрация прошла успешно! Войдите в систему.", StandardCharsets.UTF_8) + "&toastType=success";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}
