package com.example.Shop.repository;

import com.example.Shop.entity.FavoriteBrand;
import com.example.Shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteBrandRepository extends JpaRepository<FavoriteBrand, Long> {
    List<FavoriteBrand> findByUser(User user);
    Optional<FavoriteBrand> findByUserAndBrand(User user, String brand);
    boolean existsByUserAndBrand(User user, String brand);
    void deleteByUserAndBrand(User user, String brand);
}
