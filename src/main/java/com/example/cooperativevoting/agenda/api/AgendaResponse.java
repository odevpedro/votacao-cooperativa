package com.example.cooperativevoting.agenda.api;

import com.example.cooperativevoting.agenda.domain.Agenda;
import java.time.Instant;
import java.util.UUID;

public record AgendaResponse(UUID id, String title, String description, Instant createdAt) {
  public static AgendaResponse from(Agenda agenda) {
    return new AgendaResponse(
        agenda.getId(), agenda.getTitle(), agenda.getDescription(), agenda.getCreatedAt());
  }
}
