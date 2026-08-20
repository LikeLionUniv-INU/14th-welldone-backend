package com.likelion.welldone.controller;

import com.likelion.welldone.common.ApiException;
import com.likelion.welldone.common.ApiResponse;
import com.likelion.welldone.common.WellnessCategory;
import com.likelion.welldone.entity.OnboardingPreference;
import com.likelion.welldone.entity.Routine;
import com.likelion.welldone.entity.WellnessAreaSetting;
import com.likelion.welldone.repository.OnboardingPreferenceRepository;
import com.likelion.welldone.repository.RoutineRepository;
import com.likelion.welldone.repository.WellnessAreaSettingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/home")
public class HomeApiController {

  private final RoutineRepository routineRepository;
  private final OnboardingPreferenceRepository preferenceRepository;
  private final WellnessAreaSettingRepository settingRepository;
  private final ObjectMapper objectMapper = JsonMapper.builder().build();

  public HomeApiController(RoutineRepository routineRepository,
                           OnboardingPreferenceRepository preferenceRepository,
                           WellnessAreaSettingRepository settingRepository) {
    this.routineRepository = routineRepository;
    this.preferenceRepository = preferenceRepository;
    this.settingRepository = settingRepository;
  }

  // ===== 13. 설정 드로어 정보 조회 =====
  @GetMapping("/menu")
  public ApiResponse<Map<String, Object>> getMenu() {
    return ApiResponse.success(Map.of(
        "isPro", false,
        "scheduleUpdatedAt", LocalDate.now().toString()
    ));
  }

  // ===== 14. 홈 상태 조회 =====
  @GetMapping("/status")
  public ApiResponse<Map<String, Object>> getStatus() {
    LocalDate today = LocalDate.now();
    int week = today.get(WeekFields.of(Locale.KOREA).weekOfMonth());
    // TODO: 실제 근무 스케줄 기반 문구 생성 로직 ("Night 근무 중 식사 후 3시간 경과" 등)
    return ApiResponse.success(Map.of(
        "year", today.getYear(),
        "month", today.getMonthValue(),
        "week", week,
        "statusMessage", "오늘도 힘내봐요!"
    ));
  }

  // ===== 15. 웰니스 영역별 게이지 조회 =====
  @GetMapping("/wellness")
  public ApiResponse<Map<String, Object>> getWellness() throws Exception {
    OnboardingPreference pref = preferenceRepository.findById(1).orElse(null);
    List<String> categories = pref != null && pref.getCategoriesJson() != null
        ? objectMapper.readValue(pref.getCategoriesJson(), List.class)
        : List.of();

    LocalDate today = LocalDate.now();
    List<Map<String, Object>> areas = new ArrayList<>();

    for (String fullName : categories) {
      List<Routine> todayRoutines = routineRepository.findByScheduledDateOrderByScheduledTimeAsc(today).stream()
          .filter(r -> fullName.equals(r.getCategory()))
          .toList();

      int total = todayRoutines.size();
      long done = todayRoutines.stream().filter(r -> "DONE".equals(r.getStatus())).count();
      int rate = total == 0 ? 0 : (int) Math.round((done * 100.0) / total);

      areas.add(Map.of(
          "areaName", WellnessCategory.to4(fullName),
          "score", rate,
          "achievementRate", rate
      ));
    }

    return ApiResponse.success(Map.of("areas", areas));
  }

  // ===== 16. 웰니스 영역 노출 설정 변경 =====
  public record WellnessSettingsRequest(List<String> order, List<String> visible) {}

  @PatchMapping("/wellness/settings")
  public ApiResponse<Void> updateWellnessSettings(@RequestBody WellnessSettingsRequest req) throws Exception {
    if (req.visible() == null || req.visible().isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "HOME_400_1", "노출 영역은 최소 1개 이상이어야 합니다.");
    }
    WellnessAreaSetting setting = settingRepository.findById(1).orElseGet(WellnessAreaSetting::new);
    setting.setId(1);
    setting.setOrderJson(objectMapper.writeValueAsString(req.order()));
    setting.setVisibleJson(objectMapper.writeValueAsString(req.visible()));
    settingRepository.save(setting);
    return ApiResponse.success("노출 설정이 변경되었습니다.", null);
  }

  // ===== 17. 현재 루틴 조회 =====
  @GetMapping("/routine/current")
  public ApiResponse<Map<String, Object>> getCurrentRoutine() {
    Routine routine = routineRepository.findByScheduledDateOrderByScheduledTimeAsc(LocalDate.now()).stream()
        .filter(r -> !"DONE".equals(r.getStatus()) && !"HOLD".equals(r.getStatus()))
        .findFirst()
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "HOME_404_1", "현재 수행할 루틴이 없습니다."));

    boolean isVideo = "VIDEO".equals(routine.getType());

    Map<String, Object> result = new java.util.HashMap<>();
    result.put("routineId", routine.getId());
    result.put("routineName", routine.getName());
    result.put("routineType", routine.getType());
    result.put("recommendedDuration", routine.getRecommendedDuration());
    result.put("videoUrl", isVideo ? routine.getVideoUrl() : null);
    result.put("remainingTime", isVideo && routine.getScheduledTime() != null ? routine.getScheduledTime().toString() : null);
    result.put("isVerificationRequired", routine.isVerificationRequired());

    return ApiResponse.success(result);
  }

  private Routine findRoutineOrThrow(UUID id) {
    return routineRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "HOME_404_2", "해당 루틴을 찾을 수 없습니다."));
  }

  // ===== 18. 루틴 시작 =====
  @PostMapping("/routine/{routineId}/start")
  public ApiResponse<Map<String, Object>> startRoutine(@PathVariable UUID routineId) {
    Routine r = findRoutineOrThrow(routineId);
    r.setStatus("IN_PROGRESS");
    Instant now = Instant.now();
    r.setStartedAt(now);
    routineRepository.save(r);
    return ApiResponse.success("루틴을 시작했습니다.", Map.of(
        "status", "IN_PROGRESS",
        "startedAt", now.toString()
    ));
  }

  // ===== 19. 루틴 일시정지 =====
  @PostMapping("/routine/{routineId}/pause")
  public ApiResponse<Map<String, Object>> pauseRoutine(@PathVariable UUID routineId) {
    Routine r = findRoutineOrThrow(routineId);
    r.setStatus("PAUSED");
    routineRepository.save(r);
    return ApiResponse.success("루틴을 일시정지했습니다.", Map.of("status", "PAUSED"));
  }

  // ===== 20. 루틴 완료 =====
  @PostMapping("/routine/{routineId}/complete")
  public ApiResponse<Map<String, Object>> completeRoutine(@PathVariable UUID routineId) {
    Routine r = findRoutineOrThrow(routineId);
    r.setStatus("DONE");
    r.setCompletedAt(Instant.now());
    r.setCompletionMethod("COMPLETE");
    routineRepository.save(r);
    return ApiResponse.success("루틴을 완료했습니다.", Map.of(
        "status", "COMPLETED",
        "wellnessScoreDelta", 5
    ));
  }

  // ===== 21. 루틴 사진 인증 =====
  // TODO: Supabase Storage에 실제 이미지 업로드하는 로직 연결
  @PostMapping("/routine/{routineId}/photo")
  public ApiResponse<Map<String, Object>> photoVerifyRoutine(@PathVariable UUID routineId,
                                                             @RequestParam("image") MultipartFile image) {
    if (image.isEmpty() || image.getContentType() == null || !image.getContentType().startsWith("image/")) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "HOME_400_2", "이미지 파일만 업로드 가능합니다.");
    }
    Routine r = findRoutineOrThrow(routineId);
    String photoUrl = "https://cdn.welldone.app/proof/" + routineId + ".jpg"; // TODO: 실제 업로드 URL로 교체
    r.setStatus("DONE");
    r.setCompletedAt(Instant.now());
    r.setCompletionMethod("PHOTO");
    r.setPhotoUrl(photoUrl);
    routineRepository.save(r);
    return ApiResponse.success("사진 인증이 완료되었습니다.", Map.of(
        "status", "COMPLETED",
        "photoUrl", photoUrl
    ));
  }

  // ===== 22. 오늘의 일정&루틴 리스트 조회 =====
  // TODO: 근무 스케줄(출근/퇴근) 항목까지 함께 병합하는 로직 보강 필요 (현재는 루틴만 포함)
  @GetMapping("/today")
  public ApiResponse<Map<String, Object>> getToday() {
    List<Map<String, Object>> items = routineRepository.findByScheduledDateOrderByScheduledTimeAsc(LocalDate.now()).stream()
        .map(r -> {
          Map<String, Object> item = new java.util.HashMap<>();
          item.put("time", r.getScheduledTime() != null ? r.getScheduledTime().toString() : null);
          item.put("type", "ROUTINE");
          item.put("title", r.getName());
          item.put("duration", r.getDurationMinutes() != null ? r.getDurationMinutes() + "분" : null);
          item.put("isOverdue", "HOLD".equals(r.getStatus()));
          return item;
        })
        .toList();

    return ApiResponse.success(Map.of("items", items));
  }
}