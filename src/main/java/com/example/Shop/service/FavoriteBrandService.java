package com.example.Shop.service;

import com.example.Shop.entity.FavoriteBrand;
import com.example.Shop.entity.User;
import com.example.Shop.repository.FavoriteBrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteBrandService {

    private final FavoriteBrandRepository repository;

    public List<String> getFavoriteBrands(User user) {
        return repository.findByUser(user).stream()
                .map(FavoriteBrand::getBrand)
                .toList();
    }

    public boolean isFavorite(User user, String brand) {
        return repository.existsByUserAndBrand(user, brand);
    }

    @Transactional
    public void toggle(User user, String brand) {
        var existing = repository.findByUserAndBrand(user, brand);
        if (existing.isPresent()) {
            repository.delete(existing.get());
        } else {
            repository.save(FavoriteBrand.builder().user(user).brand(brand).build());
        }
    }
}
