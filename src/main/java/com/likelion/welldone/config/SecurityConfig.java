package com.likelion.welldone.config;

import com.likelion.welldone.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 프로젝트에 spring-boot-starter-security 가 포함되어 있어서, 아무 설정도 안 하면
 * Spring Security가 자동으로 모든 요청에 로그인 화면을 띄웁니다.
 *
 * 이 앱은 Spring Security의 인증 기능 대신 자체 JwtAuthFilter로 인증을 처리하므로,
 * 여기서는 Spring Security의 기본 보호를 끄고(permitAll) 세션을 사용하지 않도록(STATELESS)
 * 설정한 뒤, JwtAuthFilter를 필터 체인에 끼워 넣기만 합니다.
 * 실제 401 거부는 JwtAuthFilter가 담당합니다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;

  public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
    this.jwtAuthFilter = jwtAuthFilter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> {}) // WebConfig의 CORS 매핑을 그대로 사용
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .httpBasic(basic -> basic.disable())
        .formLogin(form -> form.disable())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}