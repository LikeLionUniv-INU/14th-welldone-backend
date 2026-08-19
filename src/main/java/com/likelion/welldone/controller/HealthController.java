package com.likelion.welldone.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

  // 배포 플랫폼(Railway 등) 헬스체크용
  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of("status", "ok");
  }
}