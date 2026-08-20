package com.likelion.welldone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "routines")
@Getter
@Setter
@NoArgsConstructor
public class Routine {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "group_name")
  private String groupName; // 예: 출근 전 / 퇴근 후 수면 골든타임 / 근무 중

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String type; // VIDEO | GENERAL

  @Column(name = "duration_minutes")
  private Integer durationMinutes;

  private String frequency;

  @Column(name = "scheduled_date")
  private LocalDate scheduledDate;

  @Column(name = "scheduled_time")
  private LocalTime scheduledTime;

  @Column(nullable = false)
  private String status = "PENDING"; // PENDING, IN_PROGRESS, PAUSED, DONE, HOLD

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  @Column(name = "completion_method")
  private String completionMethod; // COMPLETE | PHOTO

  @Column(name = "photo_url")
  private String photoUrl;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "recommended_duration")
  private String recommendedDuration; // 예: "Youtube 30분"

  @Column(name = "video_url")
  private String videoUrl;

  @Column(name = "is_verification_required", nullable = false)
  private boolean verificationRequired = false;

  @Column(name = "category")
  private String category; // 온보딩에서 선택한 웰니스 카테고리 풀네임과 매칭 (예: "신체적 건강")
}