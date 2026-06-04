package com.example.Shop.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {
    List<ProductDocument> findByNameContainingIgnoreCase(String name);
}
