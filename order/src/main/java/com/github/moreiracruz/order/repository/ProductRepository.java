package com.github.moreiracruz.order.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.github.moreiracruz.order.model.Product;

public interface ProductRepository extends ElasticsearchRepository<Product, String> {
	
}
