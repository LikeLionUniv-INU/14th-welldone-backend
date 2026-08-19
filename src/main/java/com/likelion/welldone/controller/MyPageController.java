package com.likelion.welldone.controller;

import com.likelion.welldone.entity.MonthlyReport;
import com.likelion.welldone.entity.Routine;
import com.likelion.welldone.repository.MonthlyReportRepository;
import com.likelion.welldone.repository.RoutineRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/mypage")
public class MyPageController {

  private final RoutineRepository routineRepository;
  private final MonthlyReportRepository monthlyReportRepository;

  public MyPageController(RoutineRepository routineRepository, MonthlyReportRepository monthlyReportRepository) {
    this.routineRepository = routineRepository;
    this.monthlyReportRepository = monthlyReportRepository;
  }

  // GET /api/mypage/weekly?weekStart=2026-08-17&weekEnd=2026-08-23
  @GetMapping("/weekly")
  public Map<String, Object> weekly(@RequestParam LocalDate weekStart, @RequestParam LocalDate weekEnd) {
    List<Routine> routines = routineRepository.findByScheduledDateBetween(weekStart, weekEnd);
    return Map.of("routines", routines);
  }

  // GET /api/mypage/monthly-report?month=2026-07
  // reportStatus: 'PROCESSING' | 'DONE' (매월 1일 배치 잡이 생성)
  @GetMapping("/monthly-report")
  public ResponseEntity<?> monthlyReport(@RequestParam(required = false) String month) {
    if (month == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "month 쿼리 파라미터가 필요합니다 (예: 2026-07)."));
    }

    Optional<MonthlyReport> report = monthlyReportRepository.findById(month);
    if (report.isEmpty()) {
      return ResponseEntity.ok(Map.of("reportStatus", "PROCESSING", "report", Map.of()));
    }
    return ResponseEntity.ok(Map.of("reportStatus", "DONE", "report", report.get()));
  }
}