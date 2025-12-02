package com.example.apigateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Product", description = "Product management endpoints")
public class ProductGatewayController {

    private final RestTemplate restTemplate;

    @Value("${product.service.base-url}")
    private String productServiceUrl;

    public ProductGatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/lists")
    @Operation(summary = "Get all products", description = "Retrieves a paginated list of all products. No authentication required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "503", description = "Product service unavailable", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
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
        } catch (ResourceAccessException e) {
            log.error("Product service connection error: ", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Product service is not available. Please ensure the product service is running on port 8082.");
        } catch (HttpClientErrorException e) {
            log.error("Product service error: ", e);
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error getting products: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Retrieves a paginated list of products based on a search query. Supports wildcard search (e.g., product name). No authentication required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "503", description = "Product service unavailable", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
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


        return restTemplate.getForEntity(uri, Object.class);
    }

    @GetMapping("/product-detail/{id}")
    @Operation(
            summary = "Get product detail",
            description = "Retrieves the detailed information of a specific product based on its product name. No authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "503", description = "Product service unavailable", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> getProductDetail(@PathVariable String id) {
        URI uri = UriComponentsBuilder
                .fromUriString(productServiceUrl)
                .path("/api/product/product-detail/{id}")
                .buildAndExpand(id)
                .toUri();
        return restTemplate.getForEntity(uri, Object.class);
    }
}