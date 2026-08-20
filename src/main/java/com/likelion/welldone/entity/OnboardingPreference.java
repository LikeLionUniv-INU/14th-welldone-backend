package com.likelion.welldone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "onboarding_preferences")
@Getter @Setter @NoArgsConstructor
public class OnboardingPreference {
  @Id
  private Integer id = 1;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "categories_json", columnDefinition = "jsonb")
  private String categoriesJson;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "q1_tags_json", columnDefinition = "jsonb")
  private String q1TagsJson;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "q2_tags_json", columnDefinition = "jsonb")
  private String q2TagsJson;

  @Column(name = "q3_text")
  private String q3Text;
}