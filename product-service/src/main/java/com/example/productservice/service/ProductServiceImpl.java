package com.example.productservice.service;

import com.example.productservice.dto.GetProductResponse;
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
                .map(product -> new GetProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getCategory().name(),
                        product.getPrice()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GetProductResponse> searchProducts(String productName, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(productName, pageable)
                .map(product -> new GetProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getCategory().name(),
                        product.getPrice()));
    }

    @Override
    public GetProductResponse getProductDetail(String productName) {
        return productRepository.findByName(productName)
                .map(product -> new GetProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getCategory().name(),
                        product.getPrice()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with name: " + productName
                ));
    }
}