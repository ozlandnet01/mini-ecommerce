package com.example.apigateway.controller;

import com.example.apigateway.dto.AddToCartRequest;
import com.example.apigateway.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/cart")
@Tag(name = "Cart", description = "Cart management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class CartGatewayController {

    private final RestTemplate restTemplate;

    @Value("${cart.service.base-url}")
    private String cartServiceUrl;

    public CartGatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/addToCart")
    @Operation(summary = "Add product to cart", description = "Adds a product to the user's shopping cart. If the product already exists in the cart, its quantity will be updated. Authentication required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product added to cart successfully", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Product not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "503", description = "Product service unavailable", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> addToCart(
            @RequestBody AddToCartRequest request) {

        String url = cartServiceUrl + "/api/cart/addToCart";

        Map<String, Object> body = new HashMap<>();
        body.put("memberId", request.getMemberId());
        body.put("productId", request.getProductId());
        body.put("qty", request.getQty());

        try {
            return restTemplate.postForEntity(url, body, Object.class);
        } catch (Exception e) {
            log.error("Register error: ", e);
            if (e instanceof HttpClientErrorException) {
                HttpClientErrorException ex = (HttpClientErrorException) e;
                return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/deleteFromCart/{id}")
    @Operation(summary = "Delete cart item", description = "Deletes a cart item by ID and returns whether deletion was successful. Requires authentication.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cart item deleted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cart item not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<?> deleteFromCart(
            @PathVariable String id
    ) {

        URI uri = UriComponentsBuilder
                .fromUriString(cartServiceUrl)
                .path("/api/cart/deleteFromCart/{id}")
                .buildAndExpand(id)
                .toUri();

        return restTemplate.exchange(
                uri,
                HttpMethod.DELETE,
                null,
                Boolean.class
        );
    }

    @GetMapping("/getCart")
    @Operation(summary = "Get user cart", description = "Retrieves a paginated list of all cart items for the logged-in user. Requires authentication.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> getUsers(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String userId = principal.getUserId();
        URI uri = UriComponentsBuilder
                .fromUriString(cartServiceUrl)
                .path("/api/cart/getCarts")
                .queryParam("memberId", userId)
                .queryParam("page", page)
                .queryParam("size", size)
                .build()
                .toUri();

        return restTemplate.getForEntity(uri, Object.class);
    }
}