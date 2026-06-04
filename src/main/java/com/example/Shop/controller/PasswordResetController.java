package com.example.Shop.controller;

import com.example.Shop.service.EmailService;
import com.example.Shop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final UserService userService;
    private final EmailService emailService;

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email,
                                 HttpServletRequest request,
                                 Model model) {
        try {
            String token = userService.createPasswordResetToken(email);
            String resetUrl = request.getScheme() + "://" + request.getServerName()
                    + ":" + request.getServerPort()
                    + "/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(email, email, resetUrl);
            model.addAttribute("success", true);
        } catch (RuntimeException e) {
            // Don't reveal if email exists - always show success for security
            model.addAttribute("success", true);
        }
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam String token, Model model) {
        try {
            userService.validateToken(token);
            model.addAttribute("token", token);
            return "reset-password";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "reset-password";
        }
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String password,
                                @RequestParam String passwordConfirm,
                                Model model) {
        if (!password.equals(passwordConfirm)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Passwords do not match");
            return "reset-password";
        }
        if (password.length() < 6) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Password must be at least 6 characters");
            return "reset-password";
        }
        try {
            userService.resetPassword(token, password);
            return "redirect:/login?reset";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "reset-password";
        }
    }
}
