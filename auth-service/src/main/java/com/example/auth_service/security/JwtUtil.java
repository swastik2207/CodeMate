package com.example.auth_service.security;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;
import com.example.auth_service.config.JwtConfig;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Component
public class JwtUtil {

    private final JwtConfig jwtconfig;

       @Autowired
        public JwtUtil(JwtConfig jwtConfig) {
        this.jwtconfig= jwtConfig;
    }



    public String generateToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
            .signWith(SignatureAlgorithm.HS256, jwtconfig.getSecret())
            .compact();
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtconfig.getSecret()).parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
