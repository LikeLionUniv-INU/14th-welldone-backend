package com.likelion.welldone.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  // 여러 개 주소를 콤마(,)로 구분해서 받습니다.
  @Value("${app.client-origin}")
  private String clientOrigin;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    String[] origins = clientOrigin.split(",");
    for (int i = 0; i < origins.length; i++) {
      origins[i] = origins[i].trim();
    }

    registry.addMapping("/api/**")
        .allowedOrigins(origins)
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true);
  }
}