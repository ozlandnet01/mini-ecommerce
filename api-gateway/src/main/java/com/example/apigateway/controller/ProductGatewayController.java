package com.example.apigateway.controller;

import com.example.apigateway.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/product")
@Tag(name = "Product", description = "Product management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class ProductGatewayController {

  private final RestTemplate restTemplate;

  @Value("${product.service.base-url}")
  private String productServiceUrl;

  public ProductGatewayController(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

    @GetMapping("/lists")
    @Operation(summary = "Get all product", description = "Retrieves a paginated list of all product.")
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

        return restTemplate.getForEntity(uri, Object.class);
    }
}