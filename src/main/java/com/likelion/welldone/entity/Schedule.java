package com.likelion.welldone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
public class Schedule {

  @Id
  private LocalDate date;

  @Column(name = "duty_type", nullable = false)
  private String dutyType; // D, E, N, OFF

  private String memo;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();
}