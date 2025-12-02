package com.example.apigateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for add product to cart")
public class AddToCartRequest {

    @Schema(description = "Id of the member adding items to the cart", example = "string", defaultValue = "string", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberId;

    @Schema(description = "Id of the product to be added", example = "string", defaultValue = "string", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productId;

    @Schema(description = "Quantity of the product to add", example = "1", defaultValue = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer qty;
}