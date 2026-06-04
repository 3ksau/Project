package com.example.Shop.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
public class UploadController {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @PostMapping("/admin/upload")
    public String uploadImage(@RequestParam("file") MultipartFile file,
                              @RequestParam("productId") Long productId,
                              RedirectAttributes ra) {
        if (file.isEmpty()) {
            ra.addFlashAttribute("error", "File is empty");
            return "redirect:/admin/products/" + productId + "/edit";
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            ra.addFlashAttribute("error", "Invalid file type. Allowed: JPEG, PNG, GIF, WebP");
            return "redirect:/admin/products/" + productId + "/edit";
        }

        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            ra.addFlashAttribute("error", "File is too large. Maximum size is 5MB");
            return "redirect:/admin/products/" + productId + "/edit";
        }

        try {
            Path dir = Paths.get(uploadPath);
            Files.createDirectories(dir);

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            file.transferTo(dir.resolve(filename));

            ra.addFlashAttribute("message", "Uploaded: " + filename);
            ra.addFlashAttribute("uploadedFile", "/uploads/" + filename);
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Upload failed: " + e.getMessage());
        }
        return "redirect:/admin/products/" + productId + "/edit";
    }
}
