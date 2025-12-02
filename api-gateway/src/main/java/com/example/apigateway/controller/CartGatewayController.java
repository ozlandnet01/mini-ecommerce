package com.example.apigateway.controller;

import com.example.apigateway.dto.AddToCartRequest;
import com.example.apigateway.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/cart")
@Tag(name = "Cart")
@SecurityRequirement(name = "Bearer Authentication")
public class CartGatewayController {

    private final RestTemplate restTemplate;

    @Value("${cart.service.base-url}")
    private String cartServiceUrl;

    public CartGatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private ResponseEntity<?> handleError(Exception e) {
        log.error("Cart API Error: ", e);
        if (e instanceof HttpClientErrorException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @PostMapping("/addToCart")
    @Operation(summary = "Add product to cart")
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request,
                                       @AuthenticationPrincipal UserPrincipal principal) {

        String userId = principal.getUserId();
        URI uri = UriComponentsBuilder
                .fromUriString(cartServiceUrl + "/api/cart/addToCart")
                .queryParam("memberId", userId) // add memberId as query param
                .build()
                .toUri();

        try {
            return restTemplate.postForEntity(
                    uri,
                    request,
                    Object.class
            );
        } catch (Exception e) {
            return handleError(e);
        }
    }

    @DeleteMapping("/deleteFromCart/{id}")
    @Operation(summary = "Delete cart item")
    public ResponseEntity<?> deleteFromCart(@PathVariable String id) {

        URI uri = UriComponentsBuilder
                .fromUriString(cartServiceUrl)
                .path("/api/cart/deleteFromCart/{id}")
                .buildAndExpand(id)
                .toUri();

        try {
            return restTemplate.exchange(uri, HttpMethod.DELETE, null, Boolean.class);
        } catch (Exception e) {
            return handleError(e);
        }
    }

    @GetMapping("/getCart")
    @Operation(summary = "Get user cart")
    public ResponseEntity<?> getUsers(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String userId = principal.getUserId();

        URI uri = UriComponentsBuilder
                .fromUriString(cartServiceUrl)
                .path("/api/cart/getCart")
                .queryParam("memberId", userId)
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
}