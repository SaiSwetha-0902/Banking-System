package com.bank.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.Date;

import javax.crypto.SecretKey;


@Service
public class JwtService {

// Use your Base64-encoded secure key from application.properties
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        // Decode Base64 key to SecretKey
        return Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(jwtSecret));
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

public String extractEmail(String token) {
    Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())  // Use the SecretKey, not the string
            .build()
            .parseClaimsJws(token)
            .getBody();
    return claims.getSubject();
}

public boolean validateToken(String token) {
    try {
        Jwts.parserBuilder()
            .setSigningKey(getSigningKey())  // Use the SecretKey here too
            .build()
            .parseClaimsJws(token);
        return true;
    } catch (Exception e) {
        return false;
    }
}

}