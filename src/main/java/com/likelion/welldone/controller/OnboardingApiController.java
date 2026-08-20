package com.likelion.welldone.controller;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.likelion.welldone.common.ApiException;
import com.likelion.welldone.common.ApiResponse;
import com.likelion.welldone.entity.*;
import com.likelion.welldone.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.likelion.welldone.service.GeminiService;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingApiController {

  private final ScheduleRepository scheduleRepository;
  private final WeeklyScheduleRepository weeklyScheduleRepository;
  private final OnboardingPreferenceRepository preferenceRepository;
  private final RoutineAnalysisRepository analysisRepository;
  private final RoutineRepository routineRepository;
  private final GeminiService geminiService;
  private final ObjectMapper objectMapper = JsonMapper.builder().build();

  public OnboardingApiController(ScheduleRepository scheduleRepository,
                                 WeeklyScheduleRepository weeklyScheduleRepository,
                                 OnboardingPreferenceRepository preferenceRepository,
                                 RoutineAnalysisRepository analysisRepository,
                                 RoutineRepository routineRepository,
                                 GeminiService geminiService) {
    this.scheduleRepository = scheduleRepository;
    this.weeklyScheduleRepository = weeklyScheduleRepository;
    this.preferenceRepository = preferenceRepository;
    this.analysisRepository = analysisRepository;
    this.routineRepository = routineRepository;
    this.geminiService = geminiService;
  }

  // ===== 3. 스케줄표 이미지 업로드 =====
  // TODO: 이미지를 Supabase Storage에 실제 업로드하고 OCR/AI로 파싱하는 로직 연결
  @PostMapping("/schedule/image")
  public ApiResponse<Map<String, Object>> uploadScheduleImage(
      @RequestParam("scheduleType") String scheduleType,
      @RequestParam("image") MultipartFile image) {
    if (image.isEmpty() || !List.of("image/png", "image/jpeg").contains(image.getContentType())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "ONBOARDING_400_1", "지원하지 않는 이미지 형식입니다. (PNG, JPG만 가능)");
    }
    long scheduleId = System.currentTimeMillis() % 100000;
    return ApiResponse.success("스케줄표 업로드에 성공했습니다.", Map.of(
        "scheduleId", scheduleId,
        "status", "PROCESSING"
    ));
  }

  // ===== 4. 교대근무표 수동 저장 =====
  public record DutyItem(String date, String dutyType, String memo) {}
  public record ManualScheduleRequest(List<DutyItem> duties) {}

  @PostMapping("/schedule/manual")
  public ApiResponse<Void> saveManualSchedule(@RequestBody ManualScheduleRequest req) {
    for (DutyItem d : req.duties()) {
      if (d.memo() != null && d.memo().length() > 30) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "ONBOARDING_400_2", "메모는 최대 30자까지 입력 가능합니다.");
      }
    }
    List<Schedule> entities = req.duties().stream().map(d -> {
      Schedule s = new Schedule();
      s.setDate(java.time.LocalDate.parse(d.date()));
      s.setDutyType(d.dutyType());
      s.setMemo(d.memo());
      return s;
    }).toList();
    scheduleRepository.saveAll(entities);
    return ApiResponse.success("스케줄이 저장되었습니다.", null);
  }

  // ===== 5. 주간 시간표 저장 =====
  public record WeeklyItem(String dayOfWeek, String startTime, String endTime, String title, String location) {}
  public record WeeklyScheduleRequest(List<WeeklyItem> schedules) {}

  @PostMapping("/schedule/weekly")
  public ApiResponse<Void> saveWeeklySchedule(@RequestBody WeeklyScheduleRequest req) {
    List<WeeklySchedule> entities = req.schedules().stream().map(w -> {
      LocalTime start = LocalTime.parse(w.startTime());
      LocalTime end = LocalTime.parse(w.endTime());
      if (!start.isBefore(end)) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "ONBOARDING_400_3", "시작 시간은 종료 시간보다 빨라야 합니다.");
      }
      WeeklySchedule ws = new WeeklySchedule();
      ws.setDayOfWeek(w.dayOfWeek());
      ws.setStartTime(start);
      ws.setEndTime(end);
      ws.setTitle(w.title());
      ws.setLocation(w.location());
      return ws;
    }).toList();
    weeklyScheduleRepository.saveAll(entities);
    return ApiResponse.success("주간 시간표가 저장되었습니다.", null);
  }

  // ===== 6. 웰니스 카테고리 선택 제출 =====
  public record CategoriesRequest(List<String> categories) {}

  @PostMapping("/categories")
  public ApiResponse<Void> saveCategories(@RequestBody CategoriesRequest req) throws Exception {
    if (req.categories() == null || req.categories().isEmpty() || req.categories().size() > 3) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "ONBOARDING_400_4", "카테고리는 1~3개까지 선택 가능합니다.");
    }
    OnboardingPreference pref = preferenceRepository.findById(1).orElseGet(OnboardingPreference::new);
    pref.setId(1);
    pref.setCategoriesJson(objectMapper.writeValueAsString(req.categories()));
    preferenceRepository.save(pref);
    return ApiResponse.success("카테고리 선택이 저장되었습니다.", null);
  }

  // ===== 7. 사전질문(취향 PICK) 제출 =====
  public record PreferencesRequest(List<String> q1Tags, List<String> q2Tags, String q3Text) {}

  @PostMapping("/preferences")
  public ApiResponse<Void> savePreferences(@RequestBody PreferencesRequest req) throws Exception {
    if (req.q3Text() != null && req.q3Text().length() > 200) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "ONBOARDING_400_5", "자유 텍스트는 최대 200자까지 입력 가능합니다.");
    }
    OnboardingPreference pref = preferenceRepository.findById(1).orElseGet(OnboardingPreference::new);
    pref.setId(1);
    pref.setQ1TagsJson(objectMapper.writeValueAsString(req.q1Tags()));
    pref.setQ2TagsJson(objectMapper.writeValueAsString(req.q2Tags()));
    pref.setQ3Text(req.q3Text());
    preferenceRepository.save(pref);
    return ApiResponse.success("설문 응답이 저장되었습니다.", null);
  }

  // ===== 8. AI 루틴 분석 요청 =====
  @PostMapping("/routine/generate")
  public ApiResponse<Map<String, Object>> generateRoutine() throws Exception {
    List<Schedule> schedule = scheduleRepository.findAll();
    if (schedule.isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "ONBOARDING_400_6", "스케줄 데이터가 등록되지 않았습니다.");
    }
    OnboardingPreference pref = preferenceRepository.findById(1)
        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "ONBOARDING_400_6", "선호 정보가 등록되지 않았습니다."));

    String analysisId = "an_" + java.time.LocalDate.now().toString().replace("-", "") + "_" + UUID.randomUUID().toString().substring(0, 4);

    RoutineAnalysis analysis = new RoutineAnalysis();
    analysis.setAnalysisId(analysisId);
    analysis.setStatus("PROCESSING");
    analysisRepository.save(analysis);

    // MVP: 동기 호출로 바로 처리 (실제 배치/큐 없이 즉시 완료 처리)
    try {
      JsonNode scheduleJson = objectMapper.valueToTree(schedule);
      List<String> categories = objectMapper.readValue(pref.getCategoriesJson(), List.class);
      JsonNode preferencesJson = objectMapper.createObjectNode()
          .set("q1Tags", objectMapper.readTree(pref.getQ1TagsJson() != null ? pref.getQ1TagsJson() : "[]"));

      JsonNode result = geminiService.generateRoutineSuggestion(scheduleJson, categories, preferencesJson);

      analysis.setStatus("DONE");
      analysis.setWeeklyBriefing(result.path("weeklyBriefing").asText());
      analysis.setGroupsJson(result.path("groups").toString());
      analysisRepository.save(analysis);
    } catch (Exception e) {
      e.printStackTrace(); // TODO: 디버깅 끝나면 이 줄 지우기
      analysis.setStatus("FAILED");
      analysisRepository.save(analysis);
    }

    return ApiResponse.success("AI 분석을 시작했습니다.", Map.of(
        "analysisId", analysisId,
        "status", "PROCESSING"
    ));
  }

  // ===== 9. AI 루틴 분석 상태 조회 =====
  @GetMapping("/routine/status")
  public ApiResponse<Map<String, Object>> getRoutineStatus(@RequestParam String analysisId) {
    RoutineAnalysis analysis = analysisRepository.findById(analysisId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ONBOARDING_404", "해당 분석 요청을 찾을 수 없습니다."));
    return ApiResponse.success(Map.of("status", analysis.getStatus()));
  }

  // ===== 10. AI 루틴 제안 결과 조회 =====
  @GetMapping("/routine/suggestion")
  public ApiResponse<Map<String, Object>> getRoutineSuggestion() throws Exception {
    RoutineAnalysis analysis = analysisRepository.findAll().stream()
        .filter(a -> "DONE".equals(a.getStatus()))
        .reduce((first, second) -> second) // 가장 최근 것
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ONBOARDING_404_1", "생성된 루틴 제안이 없습니다."));

    return ApiResponse.success(Map.of(
        "weeklyBriefing", analysis.getWeeklyBriefing(),
        "groups", objectMapper.readTree(analysis.getGroupsJson())
    ));
  }

  // ===== 11. AI 루틴 재추천 =====
  @PostMapping("/routine/regenerate")
  public ApiResponse<Map<String, Object>> regenerateRoutine() throws Exception {
    return generateRoutine();
  }

  // ===== 12. 루틴 적용(온보딩 완료) =====
  public record ApplyRequest(String analysisId) {}

  @PostMapping("/routine/apply")
  public ApiResponse<Map<String, Object>> applyRoutine(@RequestBody ApplyRequest req) throws Exception {
    RoutineAnalysis analysis = analysisRepository.findById(req.analysisId())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ONBOARDING_404", "해당 분석 요청을 찾을 수 없습니다."));

    if (analysis.isApplied()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "ONBOARDING_400_7", "이미 적용된 루틴입니다.");
    }

    JsonNode groups = objectMapper.readTree(analysis.getGroupsJson());
    for (JsonNode group : groups) {
      String situation = group.path("situation").asText();
      for (JsonNode r : group.path("routines")) {
        Routine routine = new Routine();
        routine.setGroupName(situation);
        routine.setName(r.path("routineName").asText());
        routine.setFrequency(r.path("cycle").asText());
        routine.setType("GENERAL");
        routine.setScheduledDate(java.time.LocalDate.now()); // 오늘 날짜로 지정해야 홈 화면에서 조회됨
        routineRepository.save(routine);
      }
    }

    analysis.setApplied(true);
    analysisRepository.save(analysis);

    return ApiResponse.success("온보딩이 완료되었습니다.", Map.of("isComplete", true));
  }
}