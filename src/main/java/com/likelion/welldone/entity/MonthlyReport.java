package com.likelion.welldone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "monthly_reports")
@Getter
@Setter
@NoArgsConstructor
public class MonthlyReport {

  @Id
  private String month; // 'YYYY-MM'

  @Column(name = "total_achievement_rate")
  private Double totalAchievementRate;

  @Column(name = "engine_mode")
  private String engineMode; // Booster | Maintain | Survival

  @Column(name = "golden_time_recovery_rate")
  private Double goldenTimeRecoveryRate;

  // 다음 달 위험 주차 정보를 JSON 문자열로 저장 (jsonb 컬럼)
  @Column(name = "next_month_risk_weeks", columnDefinition = "jsonb")
  private String nextMonthRiskWeeksJson;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();
}