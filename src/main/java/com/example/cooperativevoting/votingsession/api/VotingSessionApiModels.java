package com.example.cooperativevoting.votingsession.api;

import com.example.cooperativevoting.votingsession.domain.SessionStatus;
import com.example.cooperativevoting.votingsession.domain.VotingSession;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.UUID;

public final class VotingSessionApiModels {
  private VotingSessionApiModels() {}

  public record OpenSessionRequest(@Positive @Max(1440) Long durationMinutes) {}

  public record VotingSessionResponse(
      UUID id,
      UUID agendaId,
      Instant openedAt,
      Instant closesAt,
      long requestedDurationSeconds,
      SessionStatus status) {
    public static VotingSessionResponse from(VotingSession session, SessionStatus sessionStatus) {
      return new VotingSessionResponse(
          session.getId(),
          session.getAgenda().getId(),
          session.getOpenedAt(),
          session.getClosesAt(),
          session.getRequestedDurationSeconds(),
          sessionStatus);
    }
  }
}
