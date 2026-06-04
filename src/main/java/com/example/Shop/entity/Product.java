package com.example.Shop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;       // Название товара

    @Column(length = 2000)
    private String description;        // Подробное описание

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;           // Цена

    @Column(nullable = false)
    private Integer stockQuantity;       // Кол-во на складе

   private String imageUrl;           // Ссылка на фото

    @Column(length = 100)
    private String brand;        // Бренд

    @Column(length = 50)
    private String article;      // Артикул

    @Column(length = 50)
    private String material;     // Материал (латунь, нерж., хром, пластик)

    @Column(length = 50)
    private String color;        // Цвет/покрытие (хром, белый, матовый чёрный)

    @Column(precision = 8, scale = 2)
    private java.math.BigDecimal weight; // Вес (кг)

    @Column(length = 30)
    private String warranty;     // Гарантия (напр. "5 лет")

    private Boolean available;   // В наличии / под заказ

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}