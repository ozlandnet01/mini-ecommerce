package com.example.productservice;

import com.example.productservice.dto.GetProductResponse;
import com.example.productservice.model.Product;
import com.example.productservice.constant.Category;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.service.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private Product createDummyProduct(String id, String name) {
        return Product.builder()
                .id(id)
                .name(name)
                .description("desc " + name)
                .category(Category.ELECTRONICS)
                .price(new BigDecimal("100.00"))
                .build();
    }

    @Test
    void testGetAllProducts() {
        Product p1 = createDummyProduct("1", "Laptop");
        Product p2 = createDummyProduct("2", "Smartphone");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(p1, p2), pageable, 2);

        when(productRepository.findAll(pageable)).thenReturn(productPage);

        Page<GetProductResponse> result = productService.getAllProducts(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).name());
        verify(productRepository).findAll(pageable);
    }

    @Test
    void testSearchProducts() {
        Product p1 = createDummyProduct("1", "Laptop");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(p1), pageable, 1);

        when(productRepository.findByNameContainingIgnoreCase("lap", pageable)).thenReturn(productPage);

        Page<GetProductResponse> result = productService.searchProducts("lap", pageable);

        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).name().toLowerCase().contains("lap"));
        verify(productRepository).findByNameContainingIgnoreCase("lap", pageable);
    }

    @Test
    void testGetProductDetailFound() {
        Product p = createDummyProduct("1", "Laptop");

        when(productRepository.findById("1")).thenReturn(Optional.of(p));

        GetProductResponse response = productService.getProductDetail("1");

        assertEquals("1", response.id());
        assertEquals("Laptop", response.name());
        verify(productRepository).findById("1");
    }

    @Test
    void testGetProductDetailNotFound() {
        when(productRepository.findById("99")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            productService.getProductDetail("99");
        });

        assertEquals("404 NOT_FOUND \"Product not found with id: 99\"", exception.getMessage());
        verify(productRepository).findById("99");
    }
}