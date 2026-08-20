package com.likelion.welldone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "onboarding_preferences")
@Getter @Setter @NoArgsConstructor
public class OnboardingPreference {
  @Id
  private Integer id = 1; // 단일 마스터 계정이라 항상 1행만 사용

  @Column(name = "categories_json", columnDefinition = "jsonb")
  private String categoriesJson;

  @Column(name = "q1_tags_json", columnDefinition = "jsonb")
  private String q1TagsJson;

  @Column(name = "q2_tags_json", columnDefinition = "jsonb")
  private String q2TagsJson;

  @Column(name = "q3_text")
  private String q3Text;
}