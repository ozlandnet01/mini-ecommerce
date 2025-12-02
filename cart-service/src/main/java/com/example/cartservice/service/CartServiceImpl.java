package com.example.cartservice.service;

import com.example.cartservice.dto.AddToCartRequest;
import com.example.cartservice.dto.AddToCartResponse;
import com.example.cartservice.dto.GetCartResponse;
import lombok.RequiredArgsConstructor;
import com.example.cartservice.model.Cart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.cartservice.repository.CartRepository;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    @Override
    public AddToCartResponse addToCart(String memberId, AddToCartRequest request) {

        Cart saved;
        Optional<Cart> existing = cartRepository
                .findByMemberIdAndProductId(memberId, request.productId());

        if (existing.isPresent()) {
            Cart cart = existing.get();
            cart.setQty(request.qty());
            cart.setUpdatedAt(Instant.now());
            saved = cartRepository.save(cart);
        } else {
            Cart newCart = Cart.builder()
                    .memberId(memberId)
                    .productId(request.productId())
                    .qty(request.qty())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            saved = cartRepository.save(newCart);
        }

        return new AddToCartResponse(
                saved.getId(),
                saved.getMemberId(),
                saved.getProductId(),
                saved.getQty()
        );
    }

    @Override
    public Page<GetCartResponse> getCart(String memberId, Pageable pageable) {
        return cartRepository.findByMemberId(memberId, pageable)
                .map(cart -> new GetCartResponse(
                        cart.getId(),
                        cart.getMemberId(),
                        cart.getProductId(),
                        cart.getQty()
                ));
    }

    @Override
    public boolean deleteFromCart(String id) {
        if (cartRepository.existsById(id)) {
            cartRepository.deleteById(id);
            return true;
        }
        return false;
    }
}