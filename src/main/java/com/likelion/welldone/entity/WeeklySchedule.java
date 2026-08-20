package com.likelion.welldone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "weekly_schedules")
@Getter @Setter @NoArgsConstructor
public class WeeklySchedule {
  @Id
  @GeneratedValue
  private Long id;

  @Column(name = "day_of_week", nullable = false)
  private String dayOfWeek; // MON, TUE, WED, THU, FRI, SAT, SUN

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;

  @Column(nullable = false)
  private String title;

  private String location;
}