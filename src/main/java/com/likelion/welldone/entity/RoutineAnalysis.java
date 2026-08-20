package com.likelion.welldone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "routine_analyses")
@Getter @Setter @NoArgsConstructor
public class RoutineAnalysis {
  @Id
  private String analysisId;

  @Column(nullable = false)
  private String status; // PROCESSING | DONE | FAILED

  @Column(name = "weekly_briefing")
  private String weeklyBriefing;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "groups_json", columnDefinition = "jsonb")
  private String groupsJson;

  @Column(nullable = false)
  private boolean applied = false;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();
}