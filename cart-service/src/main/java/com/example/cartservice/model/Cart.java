package com.example.cartservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    private String id;

    private String memberId;

    private String productId;

    private Integer qty;

    private Long price;

    private Instant createdAt;

    private Instant updatedAt;
}