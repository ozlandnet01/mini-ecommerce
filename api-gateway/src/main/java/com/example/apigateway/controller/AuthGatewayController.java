package com.example.apigateway.controller;

import com.example.apigateway.dto.LoginRequest;
import com.example.apigateway.dto.RegisterRequest;
import com.example.apigateway.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication")
public class AuthGatewayController {

    private final RestTemplate restTemplate;
    private final String memberServiceBaseUrl;
    private final JwtUtil jwtUtil;

    public AuthGatewayController(RestTemplate restTemplate,
                                 @Value("${member.service.base-url}") String memberServiceBaseUrl,
                                 JwtUtil jwtUtil) {
        this.restTemplate = restTemplate;
        this.memberServiceBaseUrl = memberServiceBaseUrl;
        this.jwtUtil = jwtUtil;
    }

    private ResponseEntity<?> handleError(Exception e) {
        log.error("API error: ", e);
        if (e instanceof HttpClientErrorException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            return restTemplate.postForEntity(
                    memberServiceBaseUrl + "/api/auth/register",
                    request,
                    Object.class
            );
        } catch (Exception e) {
            return handleError(e);
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login user and return a JWT token")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    memberServiceBaseUrl + "/api/auth/login",
                    request,
                    Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return response;
            }

            Map<String, Object> data = response.getBody();
            String userId = (String) data.get("userId");

            String token = jwtUtil.generateToken(userId, Map.of("claims", data));

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "user", data
            ));

        } catch (Exception e) {
            return handleError(e);
        }
    }
}