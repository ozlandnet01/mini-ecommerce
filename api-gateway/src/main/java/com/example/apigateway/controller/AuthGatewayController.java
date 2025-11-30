package com.example.apigateway.controller;

import com.example.apigateway.dto.LoginRequest;
import com.example.apigateway.dto.RegisterRequest;
import com.example.apigateway.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication endpoints for user registration and login")
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

  @PostMapping("/register")
  @Operation(summary = "Register a new user", description = "Creates a new user account with email and password", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(name = "Register Request", value = "{\"email\": \"test@mail.com\", \"password\": \"password123\"}"))))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "User successfully registered", content = @Content(mediaType = "application/json")),
      @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(mediaType = "application/json")),
      @ApiResponse(responseCode = "409", description = "User already exists", content = @Content(mediaType = "application/json"))
  })
  public ResponseEntity<?> register(
      @RequestBody @Schema(example = "{\"email\": \"string\", \"password\": \"string\"}") RegisterRequest request) {
    String url = memberServiceBaseUrl + "/api/auth/register";

    // Convert DTO to Map for RestTemplate
    Map<String, Object> body = new HashMap<>();
    body.put("email", request.getEmail());
    body.put("password", request.getPassword());

    try {
      ResponseEntity<?> response = restTemplate.postForEntity(url, body, Object.class);

      if (response.getStatusCode().is2xxSuccessful()) {
        URI location = response.getHeaders().getLocation();
        if (location != null) {
          return ResponseEntity
              .created(location)
              .body(response.getBody());
        } else {
          try {
            String id = ((Map<String, String>) response.getBody()).get("id");
            if (id != null) {
              URI createdUri = ServletUriComponentsBuilder
                  .fromCurrentRequest()
                  .path("/{id}")
                  .buildAndExpand(id)
                  .toUri();
              return ResponseEntity.created(createdUri).body(response.getBody());
            }
          } catch (Exception e) {
            log.warn("Could not extract ID from response", e);
          }
          return ResponseEntity.ok(response.getBody());
        }
      }
      return response;
    } catch (Exception e) {
      log.error("Register error: ", e);
      if (e instanceof HttpClientErrorException) {
        HttpClientErrorException ex = (HttpClientErrorException) e;
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
      }
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  @PostMapping("/login")
  @Operation(summary = "Login user", description = "Authenticates a user and returns a JWT token", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(name = "Login Request", value = "{\"email\": \"test@mail.com\", \"password\": \"password123\"}"))))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(mediaType = "application/json")),
      @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(mediaType = "application/json")),
      @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(mediaType = "application/json"))
  })
  public ResponseEntity<?> login(
      @RequestBody @Schema(example = "{\"email\": \"string\", \"password\": \"string\"}") LoginRequest request) {
    log.info(">> login endpoint called with email: {}", request.getEmail());
    String url = memberServiceBaseUrl + "/api/auth/login";
    Map<String, Object> responseBody = new HashMap<>();

    // Convert DTO to Map for RestTemplate
    Map<String, Object> body = new HashMap<>();
    body.put("email", request.getEmail());
    body.put("password", request.getPassword());

    try {
      ResponseEntity<?> response = restTemplate.postForEntity(url, body, Map.class);

      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        String userId = ((Map<String, String>) response.getBody()).get("userId");
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("claims", response.getBody());

        String token = jwtUtil.generateToken(userId, claims);

        responseBody.put("token", token);
        responseBody.put("user", response.getBody());

        return ResponseEntity.ok(responseBody);
      }
    } catch (Exception e) {
      log.error("Login error: ", e);
      if (e instanceof HttpClientErrorException) {
        HttpClientErrorException ex = (HttpClientErrorException) e;
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
      }
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    return ResponseEntity.ok(responseBody);
  }
}