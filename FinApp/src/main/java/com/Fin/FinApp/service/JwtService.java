package com.Fin.FinApp.service;

import com.Fin.FinApp.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    // 1. Generate a token for a user
    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("userId", user.getId().toString());

        return Jwts.builder()
                .claims(extraClaims)                                          // ✅ was setClaims()
                .subject(user.getEmail())                                     // ✅ was setSubject()
                .issuedAt(new Date(System.currentTimeMillis()))               // ✅ was setIssuedAt()
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24)) // ✅ was setExpiration()
                .signWith(getSignInKey())                                      // ✅ no longer needs SignatureAlgorithm
                .compact();
    }

    // 2. Read the email from a token
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 3. Check if the token is still valid
    public boolean isTokenValid(String token, String userEmail) {
        final String email = extractEmail(token);
        return email.equals(userEmail) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()                 // ✅ was parserBuilder()
                .verifyWith(getSignInKey())                 // ✅ was setSigningKey()
                .build()
                .parseSignedClaims(token)                  // ✅ was parseClaimsJws()
                .getPayload();                             // ✅ was getBody()
        return claimsResolver.apply(claims);
    }

    private SecretKey getSignInKey() {                     // ✅ return type Key → SecretKey
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}