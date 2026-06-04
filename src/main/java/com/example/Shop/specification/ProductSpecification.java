package com.example.Shop.specification;

import com.example.Shop.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> withFilters(String query, Long categoryId,
                                                      BigDecimal priceMin, BigDecimal priceMax,
                                                      String brand, Boolean available,
                                                      String material, String color) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%"));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (priceMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), priceMin));
            }
            if (priceMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), priceMax));
            }
            if (brand != null && !brand.isBlank()) {
                predicates.add(cb.equal(root.get("brand"), brand));
            }
            if (available != null) {
                predicates.add(cb.equal(root.get("available"), available));
            }
            if (material != null && !material.isBlank()) {
                predicates.add(cb.equal(root.get("material"), material));
            }
            if (color != null && !color.isBlank()) {
                predicates.add(cb.equal(root.get("color"), color));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
