package com.likelion.welldone.controller;

import com.likelion.welldone.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsApiController {

  // ===== 32. 스케줄표 갱신 요청 =====
  @PostMapping("/schedule/update")
  public ApiResponse<Map<String, Object>> requestScheduleUpdate() {
    return ApiResponse.success("스케줄표 갱신 프로세스를 시작합니다.", Map.of("redirectStep", 1));
  }
}