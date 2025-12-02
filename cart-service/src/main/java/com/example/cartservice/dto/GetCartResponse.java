package com.example.cartservice.dto;

public record GetCartResponse(
        String id,
        String memberId,
        String productId,
        Integer qty
) {}