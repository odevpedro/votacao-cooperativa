package com.example.cooperativevoting.agenda.api;

import com.example.cooperativevoting.agenda.domain.Agenda;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class AgendaApiModels {
  private AgendaApiModels() {}

  public record CreateAgendaRequest(
      @NotBlank @Size(max = 150) String title, @Size(max = 2000) String description) {}

  public record AgendaResponse(UUID id, String title, String description, Instant createdAt) {
    public static AgendaResponse from(Agenda agenda) {
      return new AgendaResponse(
          agenda.getId(), agenda.getTitle(), agenda.getDescription(), agenda.getCreatedAt());
    }
  }
}
