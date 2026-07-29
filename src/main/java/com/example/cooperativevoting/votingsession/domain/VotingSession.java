package com.example.cooperativevoting.votingsession.domain;

import com.example.cooperativevoting.agenda.domain.Agenda;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "voting_sessions")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class VotingSession {
  @Id private UUID id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "agenda_id", nullable = false, unique = true)
  private Agenda agenda;

  @Column(name = "opened_at", nullable = false)
  private Instant openedAt;

  @Column(name = "closes_at", nullable = false)
  private Instant closesAt;

  @Column(name = "requested_duration_seconds", nullable = false)
  private long requestedDurationSeconds;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public VotingSession(
      UUID id,
      Agenda agenda,
      Instant openedAt,
      Instant closesAt,
      long requestedDurationSeconds,
      Instant createdAt) {
    if (!closesAt.isAfter(openedAt)) {
      throw new IllegalArgumentException("A duração da sessão deve ser positiva.");
    }
    this.id = id;
    this.agenda = agenda;
    this.openedAt = openedAt;
    this.closesAt = closesAt;
    this.requestedDurationSeconds = requestedDurationSeconds;
    this.createdAt = createdAt;
  }

  public SessionStatus statusAt(Instant now) {
    if (now.isBefore(openedAt)) {
      return SessionStatus.NOT_STARTED;
    }
    return now.isBefore(closesAt) ? SessionStatus.OPEN : SessionStatus.CLOSED;
  }
}
