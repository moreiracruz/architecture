package com.github.moreiracruz.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.moreiracruz.order.model.Product;
import com.github.moreiracruz.order.repository.ProductRepository;

@RestController
@RequestMapping("/api/products")
public class ProductController {
	
	@Autowired
	private ProductRepository productRepository;

	@PostMapping
	public Product create(@RequestBody Product product) {
		return productRepository.save(product);
	}

	@GetMapping("/{id}")
	public Product getById(@PathVariable String id) {
		return productRepository.findById(id).orElseThrow();
	}
	
}
