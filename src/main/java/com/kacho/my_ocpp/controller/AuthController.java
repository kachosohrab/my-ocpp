package com.kacho.my_ocpp.controller;

import com.kacho.my_ocpp.security.JwtUtility;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.Date;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtility jwtUtility;

    @Value("${jwt.secret}")
    private String secretKey;

    public AuthController(JwtUtility jwtUtility) {
        this.jwtUtility = jwtUtility;
    }

    @GetMapping("/generate-test-token")
    public ResponseEntity<String> generateTestToken(
            @RequestParam String sub, // User-defined subject
            @RequestParam(required = false, defaultValue = "3600") Long expirySeconds // Default to 1 hour
    ) {
        Key signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));

        String token = Jwts.builder()
                .setSubject(sub)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (expirySeconds * 1000)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        return ResponseEntity.ok(token);
    }
}
