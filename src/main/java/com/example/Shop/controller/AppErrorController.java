package com.example.Shop.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AppErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = status != null ? Integer.parseInt(status.toString()) : 500;

        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            model.addAttribute("message", "Страница не найдена");
            return "error/404";
        }

        model.addAttribute("message", "Произошла внутренняя ошибка сервера");
        return "error/500";
    }
}
