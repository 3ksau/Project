package com.example.Shop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFound(ResourceNotFoundException ex) {
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("message", ex.getMessage());
        return mav;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGeneral(Exception ex) {
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("message", "Произошла внутренняя ошибка сервера");
        return mav;
    }

    @RestControllerAdvice
    static class RestExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public ResponseEntity<Map<String, Object>> handleRestNotFound(ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", ex.getMessage(), "status", 404, "timestamp", LocalDateTime.now().toString()));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage(), "status", 400, "timestamp", LocalDateTime.now().toString()));
        }

        @ExceptionHandler(Exception.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public ResponseEntity<Map<String, Object>> handleRestGeneral(Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "status", 500, "timestamp", LocalDateTime.now().toString()));
        }
    }
}
