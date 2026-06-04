package com.example.Shop.repository;

import com.example.Shop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    java.util.List<Product> findByCategoryId(Long categoryId);
    java.util.List<Product> findByNameContainingIgnoreCase(String name);
    java.util.List<Product> findByBrandIn(java.util.List<String> brands);

    @org.springframework.data.jpa.repository.Query("SELECT p.name FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.name LIMIT :limit")
    java.util.List<String> findNameSuggestions(@org.springframework.data.repository.query.Param("query") String query, @org.springframework.data.repository.query.Param("limit") int limit);
}
