package com.example.productservice.repository;

import com.example.productservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {
    @Query("{ $or: [ " +
            "{ 'name': { $regex: ?0, $options: 'i' } }, " +
            "] }")
    Page<Product> findByNameContainingIgnoreCase(String searchTerm, Pageable pageable);

    Optional<Product> findByName(String productName);
}