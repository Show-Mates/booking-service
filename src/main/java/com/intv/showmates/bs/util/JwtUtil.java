package com.intv.showmates.bs.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
/**
 * @author NV
 * @version 1.0
 */
@Component
public class JwtUtil {
    private String SECRET_KEY = "sh2+3JRuzIaVMCGxBPeDMSzUFwDBLscv4R77LYntGns=";
    private Key secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY));

    private long expirationTime = 1000 * 60 * 60; // 1 hour in milliseconds

    public String generateToken(String username, List<SimpleGrantedAuthority> authorities) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", authorities.stream().map(SimpleGrantedAuthority::getAuthority).collect(Collectors.toList()));
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)  // Use the generated secure key to sign the token
                .compact();
    }


    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public List<SimpleGrantedAuthority> extractRoles(String token) {
        Claims claims = extractClaims(token);
        List<String> roles = (List<String>) claims.get("roles");
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());  // Compare token expiration with current time
    }

    public List<SimpleGrantedAuthority> getAuthoritiesFromRoles(String[] roles) {
        return Arrays.stream(roles)
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
