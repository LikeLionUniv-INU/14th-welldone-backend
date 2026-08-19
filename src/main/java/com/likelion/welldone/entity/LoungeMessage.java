package com.likelion.welldone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lounge_messages")
@Getter
@Setter
@NoArgsConstructor
public class LoungeMessage {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false)
  private String text;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();
}