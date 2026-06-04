package com.example.Shop.search;

import com.example.Shop.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnBean(ElasticsearchOperations.class)
public class SearchIndexService {

    private final ProductSearchRepository searchRepository;
    private final ProductRepository productRepository;
    private final ElasticsearchOperations operations;

    @PostConstruct
    public void indexAll() {
        IndexOperations indexOps = operations.indexOps(ProductDocument.class);
        if (indexOps.exists()) {
            indexOps.delete();
        }
        indexOps.create();
        var products = productRepository.findAll();
        var docs = products.stream()
                .map(p -> ProductDocument.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .brand(p.getBrand())
                        .article(p.getArticle())
                        .price(p.getPrice() != null ? p.getPrice().doubleValue() : null)
                        .material(p.getMaterial())
                        .color(p.getColor())
                        .available(p.getAvailable())
                        .build())
                .toList();
        searchRepository.saveAll(docs);
    }

    public List<ProductDocument> search(String query) {
        if (query == null || query.isBlank()) return List.of();
        return searchRepository.findByNameContainingIgnoreCase(query);
    }

    public void index(ProductDocument doc) {
        searchRepository.save(doc);
    }

    public void delete(Long id) {
        searchRepository.deleteById(id);
    }
}
