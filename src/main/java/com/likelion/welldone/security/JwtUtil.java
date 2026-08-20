package com.likelion.welldone.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

  @Value("${app.jwt-secret}")
  private String jwtSecret;

  private static final long ACCESS_EXPIRATION_MS = 1000L * 60 * 60 * 2;       // 2시간
  private static final long REFRESH_EXPIRATION_MS = 1000L * 60 * 60 * 24 * 14; // 14일

  private SecretKey key() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes());
  }

  public String generateAccessToken(String loginId) {
    return generateToken(loginId, ACCESS_EXPIRATION_MS);
  }

  public String generateRefreshToken(String loginId) {
    return generateToken(loginId, REFRESH_EXPIRATION_MS);
  }

  private String generateToken(String loginId, long expirationMs) {
    Date now = new Date();
    return Jwts.builder()
        .subject(loginId)
        .issuedAt(now)
        .expiration(new Date(now.getTime() + expirationMs))
        .signWith(key())
        .compact();
  }

  public String extractLoginId(String token) {
    return Jwts.parser().verifyWith(key()).build()
        .parseSignedClaims(token).getPayload().getSubject();
  }

  public boolean isValid(String token) {
    try {
      Jwts.parser().verifyWith(key()).build().parseSignedClaims(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}