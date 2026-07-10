package com.example.Shop.controller;

import com.example.Shop.entity.Order;
import com.example.Shop.entity.OrderStatus;
import com.example.Shop.service.OrderService;
import com.example.Shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @GetMapping("/orders")
    public String myOrders(Authentication auth, Model model) {
        var user = userService.findByEmail(auth.getName());
        model.addAttribute("orders", orderService.getUserOrders(user));
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(Authentication auth, @PathVariable Long id, Model model) {
        var user = userService.findByEmail(auth.getName());
        Order order = orderService.getOrderById(id);
        if (!order.getUser().getId().equals(user.getId())) {
            return "redirect:/orders";
        }
        model.addAttribute("order", order);
        model.addAttribute("items", orderService.getOrderItems(id));
        return "order-detail";
    }

    @GetMapping("/orders/tracking")
    public String orderTracking(Authentication auth, Model model) {
        var user = userService.findByEmail(auth.getName());
        model.addAttribute("orders", orderService.getUserOrders(user));
        return "orders-tracking";
    }

    @GetMapping("/admin/orders")
    public String adminOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("statuses", OrderStatus.values());
        return "admin-orders";
    }

    @PostMapping("/admin/orders/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        orderService.updateStatus(id, status);
        return "redirect:/admin/orders";
    }
}
