package com.example.cooperativevoting.agenda.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "agendas")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
public class Agenda {
  @Id private UUID id;

  @Column(nullable = false, length = 150)
  private String title;

  @Column(length = 2000)
  private String description;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
