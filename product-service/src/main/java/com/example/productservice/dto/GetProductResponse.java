package com.example.productservice.dto;

public record GetProductResponse(
        String id,
        String name,
        String description,
        String category,
        Long price
) {}