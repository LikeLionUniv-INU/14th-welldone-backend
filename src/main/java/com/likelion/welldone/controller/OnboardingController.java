package com.likelion.welldone.controller;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.likelion.welldone.entity.Routine;
import com.likelion.welldone.entity.Schedule;
import com.likelion.welldone.repository.RoutineRepository;
import com.likelion.welldone.repository.ScheduleRepository;
import com.likelion.welldone.service.AnthropicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

  private final ScheduleRepository scheduleRepository;
  private final RoutineRepository routineRepository;
  private final AnthropicService anthropicService;
  private final ObjectMapper objectMapper = JsonMapper.builder().build();

  public OnboardingController(ScheduleRepository scheduleRepository,
                              RoutineRepository routineRepository,
                              AnthropicService anthropicService) {
    this.scheduleRepository = scheduleRepository;
    this.routineRepository = routineRepository;
    this.anthropicService = anthropicService;
  }

  public record GenerateRequest(List<String> categories, Map<String, Object> preferences) {}
  public record ApplyRequest(List<WeekGroup> week1Routines) {}
  public record WeekGroup(String group, List<RoutineItem> routines) {}
  public record RoutineItem(String name, String type, Integer durationMinutes, String frequency) {}

  // POST /api/onboarding/generate
  // 기능명세서 STEP2~5: 카테고리 선택 + 사전질문 응답 -> AI 분석 -> 루틴 제안
  @PostMapping("/generate")
  public ResponseEntity<?> generate(@RequestBody GenerateRequest req) {
    if (req.categories() == null || req.categories().isEmpty() || req.categories().size() > 3) {
      return ResponseEntity.badRequest().body(Map.of("error", "카테고리는 1~3개 선택해야 합니다."));
    }

    try {
      List<Schedule> schedule = scheduleRepository.findAll();
      JsonNode scheduleJson = objectMapper.valueToTree(schedule);
      JsonNode preferencesJson = objectMapper.valueToTree(req.preferences());

      JsonNode result = anthropicService.generateWellnessRoutines(scheduleJson, req.categories(), preferencesJson);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      return ResponseEntity.status(500).body(Map.of("error", "AI 루틴 생성에 실패했습니다: " + e.getMessage()));
    }
  }

  // POST /api/onboarding/apply
  // [이대로 적용하기] 클릭 시 1주차 루틴을 실제 routines 테이블에 저장
  @PostMapping("/apply")
  public ResponseEntity<?> apply(@RequestBody ApplyRequest req) {
    if (req.week1Routines() == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "week1Routines 배열이 필요합니다."));
    }

    List<Routine> saved = req.week1Routines().stream()
        .flatMap(group -> group.routines().stream().map(item -> {
          Routine r = new Routine();
          r.setGroupName(group.group());
          r.setName(item.name());
          r.setType(item.type());
          r.setDurationMinutes(item.durationMinutes());
          r.setFrequency(item.frequency());
          return routineRepository.save(r);
        }))
        .toList();

    return ResponseEntity.status(201).body(Map.of("routines", saved));
  }
}