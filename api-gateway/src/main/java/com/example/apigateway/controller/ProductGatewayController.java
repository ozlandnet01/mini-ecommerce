package com.example.apigateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/product")
@Tag(name = "Product")
public class ProductGatewayController {

    private final RestTemplate restTemplate;

    @Value("${product.service.base-url}")
    private String productServiceUrl;

    public ProductGatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private ResponseEntity<?> handleError(Exception e) {
        if (e instanceof ResourceAccessException) {
            log.error("Product service unreachable: ", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Product service unavailable. Make sure it is running.");
        }

        if (e instanceof HttpClientErrorException ex) {
            log.error("Product service error: ", e);
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }

        log.error("Unexpected product service error: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected error: " + e.getMessage());
    }

    @GetMapping("/lists")
    @Operation(summary = "Get all products")
    public ResponseEntity<?> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        URI uri = UriComponentsBuilder
                .fromUriString(productServiceUrl)
                .path("/api/product/lists")
                .queryParam("page", page)
                .queryParam("size", size)
                .build()
                .toUri();

        try {
            return restTemplate.getForEntity(uri, Object.class);
        } catch (Exception e) {
            return handleError(e);
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search products")
    public ResponseEntity<?> searchProducts(
            @RequestParam(defaultValue = "string") String productName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        URI uri = UriComponentsBuilder
                .fromUriString(productServiceUrl)
                .path("/api/product/search")
                .queryParam("productName", productName)
                .queryParam("page", page)
                .queryParam("size", size)
                .build()
                .toUri();

        try {
            return restTemplate.getForEntity(uri, Object.class);
        } catch (Exception e) {
            return handleError(e);
        }
    }

    @GetMapping("/product-detail/{id}")
    @Operation(summary = "Get product detail")
    public ResponseEntity<?> getProductDetail(@PathVariable String id) {

        URI uri = UriComponentsBuilder
                .fromUriString(productServiceUrl)
                .path("/api/product/product-detail/{id}")
                .buildAndExpand(id)
                .toUri();

        try {
            return restTemplate.getForEntity(uri, Object.class);
        } catch (Exception e) {
            return handleError(e);
        }
    }
}