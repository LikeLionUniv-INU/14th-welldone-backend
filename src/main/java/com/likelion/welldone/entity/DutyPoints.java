package com.likelion.welldone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "duty_points")
@Getter
@Setter
@NoArgsConstructor
public class DutyPoints {

  @Id
  private Integer id = 1; // 단일 마스터 계정이라 항상 id=1 행 하나만 사용

  @Column(nullable = false)
  private Integer balance = 0;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();
}