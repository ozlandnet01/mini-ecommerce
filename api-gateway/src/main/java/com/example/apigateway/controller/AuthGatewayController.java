package com.example.apigateway.controller;

import com.example.apigateway.util.JwtUtil;
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
  public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
    String url = memberServiceBaseUrl + "/api/auth/register";
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
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody Map<String, Object> body) {
    String url = memberServiceBaseUrl + "/api/auth/login";
    Map<String, Object> responseBody = new HashMap<>();

    try{
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
    }catch (Exception e){
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