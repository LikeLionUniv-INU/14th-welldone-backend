package com.likelion.welldone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "wellness_area_settings")
@Getter @Setter @NoArgsConstructor
public class WellnessAreaSetting {
  @Id
  private Integer id = 1;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "order_json", columnDefinition = "jsonb")
  private String orderJson;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "visible_json", columnDefinition = "jsonb")
  private String visibleJson;
}