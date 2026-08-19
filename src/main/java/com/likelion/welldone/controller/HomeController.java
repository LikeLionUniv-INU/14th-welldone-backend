package com.likelion.welldone.controller;

import com.likelion.welldone.entity.Routine;
import com.likelion.welldone.repository.RoutineRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/home")
public class HomeController {

  private final RoutineRepository routineRepository;

  public HomeController(RoutineRepository routineRepository) {
    this.routineRepository = routineRepository;
  }

  // GET /api/home/today
  // NOW 배지 문구, 웰니스 6개 영역 게이지, 오늘의 대표 루틴을 한 번에 반환
  @GetMapping("/today")
  public Map<String, Object> today() {
    List<Routine> routines = routineRepository.findByScheduledDateOrderByScheduledTimeAsc(LocalDate.now());

    return Map.of(
        "nowBadgeText", "", // TODO: 근무 스케줄 기반 "Night 근무 중 식사 후 3시간 경과" 문구 생성 로직
        "wellnessGauges", List.of(), // TODO: 6개 영역별 완료율
        "currentRoutine", routines.isEmpty() ? null : routines.get(0),
        "upcomingRoutines", routines.isEmpty() ? List.of() : routines.subList(1, routines.size())
    );
  }

  private Routine findOrThrow(UUID id) {
    return routineRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("루틴을 찾을 수 없습니다: " + id));
  }

  // POST /api/home/routines/{id}/start
  @PostMapping("/routines/{id}/start")
  public ResponseEntity<?> start(@PathVariable UUID id) {
    Routine r = findOrThrow(id);
    r.setStatus("IN_PROGRESS");
    r.setStartedAt(Instant.now());
    routineRepository.save(r);
    return ResponseEntity.ok(Map.of("ok", true));
  }

  // POST /api/home/routines/{id}/pause
  @PostMapping("/routines/{id}/pause")
  public ResponseEntity<?> pause(@PathVariable UUID id) {
    Routine r = findOrThrow(id);
    r.setStatus("PAUSED");
    routineRepository.save(r);
    return ResponseEntity.ok(Map.of("ok", true));
  }

  public record CompleteRequest(String method, String photoUrl) {}

  // POST /api/home/routines/{id}/complete   body: { method: 'COMPLETE' | 'PHOTO', photoUrl? }
  @PostMapping("/routines/{id}/complete")
  public ResponseEntity<?> complete(@PathVariable UUID id, @RequestBody CompleteRequest req) {
    Routine r = findOrThrow(id);
    r.setStatus("DONE");
    r.setCompletedAt(Instant.now());
    r.setCompletionMethod(req.method());
    r.setPhotoUrl(req.photoUrl());
    routineRepository.save(r);
    // TODO: 듀티 포인트 적립 로직 연결
    return ResponseEntity.ok(Map.of("ok", true));
  }

  // POST /api/home/routines/{id}/hold  (미달성 자동 보류 처리)
  @PostMapping("/routines/{id}/hold")
  public ResponseEntity<?> hold(@PathVariable UUID id) {
    Routine r = findOrThrow(id);
    r.setStatus("HOLD");
    routineRepository.save(r);
    return ResponseEntity.ok(Map.of("ok", true));
  }
}