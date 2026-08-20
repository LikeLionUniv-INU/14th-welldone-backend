package com.likelion.welldone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "login_id", nullable = false, unique = true)
  private String loginId;

  @Column(nullable = false)
  private String password;

  @Column(name = "refresh_token")
  private String refreshToken;

  @Column(name = "is_onboarding_complete", nullable = false)
  private boolean onboardingComplete = false;
}