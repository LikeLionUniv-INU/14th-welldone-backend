package com.likelion.welldone.controller;

import com.likelion.welldone.common.ApiException;
import com.likelion.welldone.common.ApiResponse;
import com.likelion.welldone.common.WellnessCategory;
import com.likelion.welldone.entity.MonthlyReport;
import com.likelion.welldone.entity.Routine;
import com.likelion.welldone.repository.MonthlyReportRepository;
import com.likelion.welldone.repository.RoutineRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/my")
public class MyPageApiController {

  private final RoutineRepository routineRepository;
  private final MonthlyReportRepository monthlyReportRepository;
  private final ObjectMapper objectMapper = JsonMapper.builder().build();

  public MyPageApiController(RoutineRepository routineRepository, MonthlyReportRepository monthlyReportRepository) {
    this.routineRepository = routineRepository;
    this.monthlyReportRepository = monthlyReportRepository;
  }

  // ===== 23. 주간 루틴 수행 내역 조회 =====
  @GetMapping("/weekly")
  public ApiResponse<Map<String, Object>> getWeekly() {
    LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate sunday = monday.plusDays(6);

    List<Routine> weekRoutines = routineRepository.findByScheduledDateBetween(monday, sunday);

    Map<String, List<Routine>> grouped = weekRoutines.stream()
        .collect(Collectors.groupingBy(Routine::getName, LinkedHashMap::new, Collectors.toList()));

    List<Map<String, Object>> result = grouped.entrySet().stream().map(entry -> {
      List<Routine> items = entry.getValue();
      Boolean[] checks = new Boolean[7];
      Arrays.fill(checks, false);
      for (Routine r : items) {
        int dayIndex = r.getScheduledDate().getDayOfWeek().getValue() - 1; // MON=0 ... SUN=6
        checks[dayIndex] = "DONE".equals(r.getStatus());
      }
      String cycle = items.isEmpty() ? "" : (items.get(0).getFrequency() != null ? items.get(0).getFrequency() : "");
      return Map.<String, Object>of(
          "routineName", entry.getKey(),
          "cycle", cycle,
          "checks", checks
      );
    }).toList();

    return ApiResponse.success(Map.of("routines", result));
  }

  private String lastMonthKey() {
    return LocalDate.now().minusMonths(1).toString().substring(0, 7); // YYYY-MM
  }

  private int lastMonthNumber() {
    return LocalDate.now().minusMonths(1).getMonthValue();
  }

  // ===== 24. 월간 웰니스 리포트 조회 =====
  @GetMapping("/report/monthly")
  public ApiResponse<Map<String, Object>> getMonthlyReport() {
    Optional<MonthlyReport> reportOpt = monthlyReportRepository.findById(lastMonthKey());

    if (reportOpt.isEmpty()) {
      Map<String, Object> processing = new HashMap<>();
      processing.put("reportStatus", "PROCESSING");
      processing.put("month", lastMonthNumber());
      processing.put("achievementRate", null);
      processing.put("activeDays", null);
      processing.put("badge", null);
      processing.put("badgeLabel", null);
      processing.put("badgeMessage", null);
      return ApiResponse.success(processing);
    }

    MonthlyReport r = reportOpt.get();
    return ApiResponse.success(Map.of(
        "reportStatus", "DONE",
        "month", lastMonthNumber(),
        "achievementRate", r.getAchievementRate(),
        "activeDays", r.getActiveDays(),
        "badge", r.getBadge(),
        "badgeLabel", r.getBadgeLabel(),
        "badgeMessage", r.getBadgeMessage()
    ));
  }

  // ===== 25. 카테고리별 달성 요약 조회 =====
  @GetMapping("/report/category")
  public ApiResponse<Map<String, Object>> getCategoryReport() throws Exception {
    Optional<MonthlyReport> reportOpt = monthlyReportRepository.findById(lastMonthKey());

    if (reportOpt.isEmpty() || reportOpt.get().getCategorySummaryJson() == null) {
      Map<String, Object> processing = new HashMap<>();
      processing.put("reportStatus", "PROCESSING");
      processing.put("categories", List.of());
      processing.put("highest", null);
      processing.put("lowest", null);
      return ApiResponse.success(processing);
    }

    JsonNode raw = objectMapper.readTree(reportOpt.get().getCategorySummaryJson());
    List<Map<String, Object>> categories = new ArrayList<>();
    for (JsonNode node : raw) {
      String fullName = node.path("category").asText();
      int rate = node.path("achievementRate").asInt();
      categories.add(Map.of("name", WellnessCategory.to2(fullName), "achievementRate", rate));
    }

    if (categories.isEmpty()) {
      return ApiResponse.success(Map.of("reportStatus", "DONE", "categories", categories, "highest", Map.of(), "lowest", Map.of()));
    }

    Map<String, Object> highest = categories.stream()
        .max(Comparator.comparingInt(c -> (int) c.get("achievementRate")))
        .map(c -> Map.<String, Object>of("name", c.get("name")))
        .orElse(null);

    Map<String, Object> lowest = categories.stream()
        .min(Comparator.comparingInt(c -> (int) c.get("achievementRate")))
        .orElse(null);

    return ApiResponse.success(Map.of(
        "reportStatus", "DONE",
        "categories", categories,
        "highest", highest,
        "lowest", lowest
    ));
  }

  // ===== 26. 골든타임 회복률 조회 =====
  @GetMapping("/report/golden-time")
  public ApiResponse<Map<String, Object>> getGoldenTimeReport() {
    Optional<MonthlyReport> reportOpt = monthlyReportRepository.findById(lastMonthKey());

    if (reportOpt.isEmpty()) {
      Map<String, Object> processing = new HashMap<>();
      processing.put("reportStatus", "PROCESSING");
      processing.put("recoveryRate", null);
      processing.put("guideMessage", null);
      return ApiResponse.success(processing);
    }

    MonthlyReport r = reportOpt.get();
    return ApiResponse.success(Map.of(
        "reportStatus", "DONE",
        "recoveryRate", r.getRecoveryRate(),
        "guideMessage", r.getGuideMessage()
    ));
  }

  // ===== 27. 다음달 회복 포인트 조회 =====
  @GetMapping("/report/next-month")
  public ApiResponse<Map<String, Object>> getNextMonthReport() throws Exception {
    Optional<MonthlyReport> reportOpt = monthlyReportRepository.findById(lastMonthKey());

    if (reportOpt.isEmpty() || reportOpt.get().getRiskPeriodsJson() == null) {
      return ApiResponse.success(Map.of("reportStatus", "PROCESSING", "riskPeriods", List.of()));
    }

    JsonNode raw = objectMapper.readTree(reportOpt.get().getRiskPeriodsJson());
    return ApiResponse.success(Map.of("reportStatus", "DONE", "riskPeriods", raw));
  }
}