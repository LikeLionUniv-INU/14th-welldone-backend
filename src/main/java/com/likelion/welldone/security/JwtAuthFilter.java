package com.likelion.welldone.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * /api/** 요청 중 로그인(/api/auth/login)과 헬스체크(/health)를 제외한 모든 요청에서
 * Authorization: Bearer <token> 헤더를 검증합니다.
 * 단일 마스터 계정 구조라 Spring Security의 인증 기능 대신 이 필터가 직접 인증을 처리합니다.
 * (SecurityConfig에서 이 필터를 필터 체인에 등록합니다.)
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;

  public JwtAuthFilter(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  private static final String[] PUBLIC_PATHS = {"/api/auth/login", "/api/auth/signup", "/health"};

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    for (String publicPath : PUBLIC_PATHS) {
      if (path.equals(publicPath)) return true;
    }
    return !path.startsWith("/api/");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증 토큰이 필요합니다.");
      return;
    }

    String token = authHeader.substring(7);
    if (!jwtUtil.isValid(token)) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않거나 만료된 토큰입니다.");
      return;
    }

    request.setAttribute("loginId", jwtUtil.extractLoginId(token));
    chain.doFilter(request, response);
  }
}