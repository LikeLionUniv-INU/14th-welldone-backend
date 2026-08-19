package com.likelion.welldone.controller;

import com.likelion.welldone.entity.Schedule;
import com.likelion.welldone.repository.ScheduleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

  private final ScheduleRepository scheduleRepository;

  public ScheduleController(ScheduleRepository scheduleRepository) {
    this.scheduleRepository = scheduleRepository;
  }

  public record ScheduleEntryRequest(LocalDate date, String dutyType, String memo) {}

  // GET /api/schedule?month=2026-08
  @GetMapping
  public List<Schedule> getSchedules(@RequestParam(required = false) String month) {
    if (month == null) {
      return scheduleRepository.findAll();
    }
    YearMonth ym = YearMonth.parse(month);
    return scheduleRepository.findByDateBetweenOrderByDateAsc(ym.atDay(1), ym.atEndOfMonth());
  }

  // POST /api/schedule/manual  (기능명세서 6-1: 스케줄표 직접 제작 - D/E/N/OFF 입력)
  @PostMapping("/manual")
  public ResponseEntity<?> saveManualSchedule(@RequestBody List<ScheduleEntryRequest> entries) {
    if (entries == null || entries.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "entries 배열이 필요합니다."));
    }

    List<Schedule> saved = entries.stream().map(e -> {
      Schedule s = new Schedule();
      s.setDate(e.date());
      s.setDutyType(e.dutyType());
      s.setMemo(e.memo());
      s.setUpdatedAt(Instant.now());
      return scheduleRepository.save(s);
    }).toList();

    return ResponseEntity.status(201).body(Map.of("schedules", saved));
  }

  // POST /api/schedule/upload  (기능명세서 STEP1: 사진 촬영 / 갤러리 업로드)
  // TODO: 이미지를 Supabase Storage(schedule-images 버킷)에 업로드하고,
  //       OCR 또는 Anthropic 이미지 입력으로 스케줄을 파싱해 schedules 테이블에 저장하는 로직 연결
  @PostMapping("/upload")
  public ResponseEntity<?> uploadScheduleImage(@RequestParam("image") MultipartFile image) {
    if (image.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "이미지 파일이 필요합니다."));
    }
    return ResponseEntity.accepted().body(Map.of(
        "message", "이미지가 업로드되었습니다. 파싱이 완료되면 스케줄이 등록됩니다.",
        "filename", image.getOriginalFilename()
    ));
  }
}