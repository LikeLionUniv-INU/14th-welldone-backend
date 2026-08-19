package com.likelion.welldone.controller;

import com.likelion.welldone.entity.Schedule;
import com.likelion.welldone.repository.ScheduleRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

  private final ScheduleRepository scheduleRepository;

  public SettingsController(ScheduleRepository scheduleRepository) {
    this.scheduleRepository = scheduleRepository;
  }

  // GET /api/settings  (마지막 스케줄표 업데이트일 등)
  @GetMapping
  public Map<String, Object> settings() {
    List<Schedule> latest = scheduleRepository.findAll(
        PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "updatedAt"))
    ).getContent();

    return Map.of("lastScheduleUpdate", latest.isEmpty() ? null : latest.get(0).getUpdatedAt());
  }
}