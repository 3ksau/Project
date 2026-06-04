package com.example.Shop.search;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "russian")
    private String name;

    @Field(type = FieldType.Text, analyzer = "russian")
    private String description;

    @Field(type = FieldType.Keyword)
    private String brand;

    @Field(type = FieldType.Keyword)
    private String article;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Keyword)
    private String material;

    @Field(type = FieldType.Keyword)
    private String color;

    @Field(type = FieldType.Boolean)
    private Boolean available;
}
