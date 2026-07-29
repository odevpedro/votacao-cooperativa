package com.example.cooperativevoting.agenda.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agendas")
public class Agenda {
  @Id private UUID id;

  @Column(nullable = false, length = 150)
  private String title;

  @Column(length = 2000)
  private String description;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected Agenda() {}

  public Agenda(UUID id, String title, String description, Instant createdAt) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
