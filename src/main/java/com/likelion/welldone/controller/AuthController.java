package com.likelion.welldone.controller;

import com.likelion.welldone.common.ApiException;
import com.likelion.welldone.common.ApiResponse;
import com.likelion.welldone.entity.User;
import com.likelion.welldone.repository.UserRepository;
import com.likelion.welldone.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserRepository userRepository;
  private final JwtUtil jwtUtil;

  public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
    this.userRepository = userRepository;
    this.jwtUtil = jwtUtil;
  }

  public record SignupRequest(String loginId, String password, String passwordCheck) {}
  public record LoginRequest(String loginId, String password) {}

  // 1. POST /api/auth/signup
  @PostMapping("/signup")
  public ApiResponse<Map<String, Object>> signup(@RequestBody SignupRequest req) {
    User user = new User();
    user.setLoginId(req.loginId());
    user.setPassword(req.password());
    user = userRepository.save(user);
    return ApiResponse.success("회원가입에 성공했습니다.", Map.of("userId", user.getId()));
  }

  // 2. POST /api/auth/login
  @PostMapping("/login")
  public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest req) {
    User user = userRepository.findByLoginId(req.loginId())
        .filter(u -> u.getPassword().equals(req.password()))
        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_401", "아이디 또는 비밀번호가 일치하지 않습니다."));

    String accessToken = jwtUtil.generateAccessToken(user.getLoginId());
    String refreshToken = jwtUtil.generateRefreshToken(user.getLoginId());
    user.setRefreshToken(refreshToken);
    userRepository.save(user);

    return ApiResponse.success("로그인에 성공했습니다.", Map.of(
        "accessToken", accessToken,
        "refreshToken", refreshToken,
        "isOnboardingComplete", user.isOnboardingComplete()
    ));
  }
}