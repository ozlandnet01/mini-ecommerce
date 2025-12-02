package com.example.productservice.service;

import com.example.productservice.dto.GetProductResponse;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<GetProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GetProductResponse> searchProducts(String productName, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(productName, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public GetProductResponse getProductDetail(String id) {
        return productRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with id: " + id));
    }

    private GetProductResponse toResponse(Product product) {
        return new GetProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory().name(),
                product.getPrice()
        );
    }
}