package com.example.api_gateway_service.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class JwtUtil {

    
    @Value("${jwt.secret}")
    private String SECRET;

    /**
     * Validate the JWT token.
     * Throws exception if token is invalid.
     */
    public void validateToken(String token) {
        Jwts.parser()
            .setSigningKey(SECRET)
            .parseClaimsJws(token);
    }

    /**
     * Extract user id (subject) from the JWT token.
     */
    public String extractUserId(String token) {
        Claims claims = Jwts.parser()
                            .setSigningKey(SECRET)
                            .parseClaimsJws(token)
                            .getBody();
        return claims.getSubject();
    }
}
