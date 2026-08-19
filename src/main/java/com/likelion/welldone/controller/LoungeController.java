package com.likelion.welldone.controller;

import com.likelion.welldone.entity.DutyPoints;
import com.likelion.welldone.entity.LoungeMessage;
import com.likelion.welldone.entity.Schedule;
import com.likelion.welldone.repository.DutyPointsRepository;
import com.likelion.welldone.repository.LoungeMessageRepository;
import com.likelion.welldone.repository.ScheduleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/lounge")
public class LoungeController {

  private final ScheduleRepository scheduleRepository;
  private final LoungeMessageRepository loungeMessageRepository;
  private final DutyPointsRepository dutyPointsRepository;

  public LoungeController(ScheduleRepository scheduleRepository,
                          LoungeMessageRepository loungeMessageRepository,
                          DutyPointsRepository dutyPointsRepository) {
    this.scheduleRepository = scheduleRepository;
    this.loungeMessageRepository = loungeMessageRepository;
    this.dutyPointsRepository = dutyPointsRepository;
  }

  // GET /api/lounge/team-goal
  // 오늘 근무 형태(D/E/N/OFF) 기준 팀 자동 배정 + 팀 평균 달성률
  @GetMapping("/team-goal")
  public Map<String, Object> teamGoal() {
    Optional<Schedule> today = scheduleRepository.findById(LocalDate.now());
    String dutyType = today.map(Schedule::getDutyType).orElse(null);

    Map<String, Object> result = new HashMap<>();
    if (dutyType == null || dutyType.equals("OFF")) {
      result.put("group", null);
      result.put("teamAchievementRate", null);
      return result;
    }

    // TODO: 실제 팀원 평균 달성률 집계 쿼리로 교체
    result.put("group", "Team " + dutyType);
    result.put("teamAchievementRate", 0);
    result.put("participantCount", 0);
    result.put("goalRate", 80);
    result.put("rewardPoints", 100);
    return result;
  }

  // GET /api/lounge/chat?since=2026-08-19T10:00:00Z  (5~10초 폴링 방식)
  @GetMapping("/chat")
  public Map<String, Object> getChat(@RequestParam(required = false) String since) {
    List<LoungeMessage> messages = since != null
        ? loungeMessageRepository.findByCreatedAtAfterOrderByCreatedAtAsc(Instant.parse(since))
        : loungeMessageRepository.findTop100ByOrderByCreatedAtAsc();
    return Map.of("messages", messages);
  }

  public record ChatRequest(String text) {}

  // POST /api/lounge/chat
  @PostMapping("/chat")
  public ResponseEntity<?> postChat(@RequestBody ChatRequest req) {
    if (req.text() == null || req.text().isBlank()) {
      return ResponseEntity.badRequest().body(Map.of("error", "메시지 내용이 필요합니다."));
    }
    LoungeMessage msg = new LoungeMessage();
    msg.setText(req.text().trim());
    loungeMessageRepository.save(msg);
    return ResponseEntity.status(201).body(Map.of("message", msg));
  }

  // GET /api/lounge/points
  @GetMapping("/points")
  public Map<String, Object> points() {
    int balance = dutyPointsRepository.findById(1)
        .map(DutyPoints::getBalance)
        .orElse(0);
    return Map.of("balance", balance);
  }
}