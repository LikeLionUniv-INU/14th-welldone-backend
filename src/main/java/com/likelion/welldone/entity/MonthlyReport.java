package com.likelion.welldone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "monthly_reports")
@Getter @Setter @NoArgsConstructor
public class MonthlyReport {

  @Id
  private String month; // 내부 저장용 'YYYY-MM'

  @Column(name = "achievement_rate")
  private Double achievementRate;

  @Column(name = "active_days")
  private Integer activeDays;

  @Column(nullable = false)
  private String badge; // BOOSTER | MAINTAIN | SURVIVAL

  @Column(name = "badge_label")
  private String badgeLabel;

  @Column(name = "badge_message")
  private String badgeMessage;

  @Column(name = "recovery_rate")
  private Double recoveryRate; // 골든타임 회복률

  @Column(name = "guide_message")
  private String guideMessage;

  // [{ "category": "신체적 건강", "achievementRate": 90 }, ...] (풀네임으로 저장, 응답 시 2글자로 변환)
  @Column(name = "category_summary", columnDefinition = "jsonb")
  private String categorySummaryJson;

  // [{ "label": "7월 2주차 야간근무", "reason": "...", "tip": "..." }, ...]
  @Column(name = "risk_periods", columnDefinition = "jsonb")
  private String riskPeriodsJson;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();
}