package com.likelion.welldone.controller;

import com.likelion.welldone.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final JwtUtil jwtUtil;

  @Value("${app.master-username}")
  private String masterUsername;

  @Value("${app.master-password}")
  private String masterPassword;

  public AuthController(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  public record LoginRequest(String username, String password) {}

  // POST /api/auth/login
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest req) {
    if (req.username() == null || req.password() == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "아이디와 비밀번호를 입력해주세요."));
    }

    if (!req.username().equals(masterUsername) || !req.password().equals(masterPassword)) {
      // 기능명세서: "아이디 또는 비밀번호가 일치하지 않습니다" 문구와 매핑
      return ResponseEntity.status(401).body(Map.of("error", "아이디 또는 비밀번호가 일치하지 않습니다."));
    }

    String token = jwtUtil.generateToken(req.username());
    return ResponseEntity.ok(Map.of("token", token));
  }

  // GET /api/auth/me
  @GetMapping("/me")
  public ResponseEntity<?> me(HttpServletRequest request) {
    return ResponseEntity.ok(Map.of("username", request.getAttribute("username")));
  }
}