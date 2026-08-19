package com.likelion.welldone.service;

import tools.jackson.databind.JsonNode;
import com.likelion.welldone.entity.MonthlyReport;
import com.likelion.welldone.entity.Routine;
import com.likelion.welldone.repository.MonthlyReportRepository;
import com.likelion.welldone.repository.RoutineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 매월 1일 00:00 에 실행되는 배치 잡: 지난 달 활동 데이터를 집계해 AI 리포트를 생성하고 저장합니다.
 * (기능명세서: 월간 웰니스 리포트, reportStatus PROCESSING -> DONE)
 */
@Service
public class MonthlyReportJob {

  private static final Logger log = LoggerFactory.getLogger(MonthlyReportJob.class);

  private final RoutineRepository routineRepository;
  private final MonthlyReportRepository monthlyReportRepository;
  private final AnthropicService anthropicService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public MonthlyReportJob(RoutineRepository routineRepository,
                          MonthlyReportRepository monthlyReportRepository,
                          AnthropicService anthropicService) {
    this.routineRepository = routineRepository;
    this.monthlyReportRepository = monthlyReportRepository;
    this.anthropicService = anthropicService;
  }

  // 초 분 시 일 월 요일 -> 매월 1일 00:00:00
  @Scheduled(cron = "0 0 0 1 * *")
  public void run() {
    YearMonth lastMonth = YearMonth.from(LocalDate.now().minusMonths(1));
    String monthKey = lastMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));

    log.info("[monthlyReport] {} 리포트 생성 시작", monthKey);

    try {
      // TODO: 실제 지난 달 routine 완료 데이터 집계 쿼리로 교체
      List<Routine> routines = routineRepository.findByScheduledDateBetween(
          lastMonth.atDay(1), lastMonth.atEndOfMonth());

      long completed = routines.stream().filter(r -> "DONE".equals(r.getStatus())).count();
      Map<String, Object> userSummary = Map.of(
          "totalRoutines", routines.size(),
          "completedRoutines", completed
      );

      JsonNode result = anthropicService.generateMonthlyReport(userSummary);

      MonthlyReport report = monthlyReportRepository.findById(monthKey).orElseGet(MonthlyReport::new);
      report.setMonth(monthKey);
      report.setTotalAchievementRate(result.path("totalAchievementRate").asDouble());
      report.setEngineMode(result.path("engineMode").asText());
      report.setGoldenTimeRecoveryRate(result.path("goldenTimeRecoveryRate").asDouble());
      report.setNextMonthRiskWeeksJson(result.path("nextMonthRiskWeeks").toString());

      monthlyReportRepository.save(report);
      log.info("[monthlyReport] {} 리포트 생성 완료", monthKey);
    } catch (Exception e) {
      log.error("[monthlyReport] {} 리포트 생성 실패", monthKey, e);
    }
  }
}