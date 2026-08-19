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

  @Value("${app.jwt-expiration-ms}")
  private long jwtExpirationMs;

  private SecretKey key() {
    // jjwt 0.12.x 는 HS256 기준 최소 32바이트 이상의 시크릿을 요구합니다.
    // .env 의 JWT_SECRET 을 충분히 긴 랜덤 문자열로 설정하세요.
    return Keys.hmacShaKeyFor(jwtSecret.getBytes());
  }

  public String generateToken(String username) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + jwtExpirationMs);

    return Jwts.builder()
        .subject(username)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(key())
        .compact();
  }

  public String extractUsername(String token) {
    return Jwts.parser()
        .verifyWith(key())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
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