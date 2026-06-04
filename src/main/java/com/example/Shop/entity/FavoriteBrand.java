package com.example.Shop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "favorite_brands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteBrand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String brand;
}
