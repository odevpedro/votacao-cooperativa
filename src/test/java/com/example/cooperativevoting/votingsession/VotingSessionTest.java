package com.example.cooperativevoting.votingsession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.cooperativevoting.agenda.domain.Agenda;
import com.example.cooperativevoting.votingsession.domain.SessionStatus;
import com.example.cooperativevoting.votingsession.domain.VotingSession;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VotingSessionTest {
  private static final Instant OPENED_AT = Instant.parse("2026-07-28T16:00:00Z");
  private final Agenda agenda = new Agenda(UUID.randomUUID(), "Pauta", null, OPENED_AT);

  @Test
  void calculatesNotStartedOpenAndClosedFromTimestamps() {
    var session =
        new VotingSession(
            UUID.randomUUID(), agenda, OPENED_AT, OPENED_AT.plusSeconds(60), 60, OPENED_AT);

    assertThat(session.statusAt(OPENED_AT.minusNanos(1))).isEqualTo(SessionStatus.NOT_STARTED);
    assertThat(session.statusAt(OPENED_AT)).isEqualTo(SessionStatus.OPEN);
    assertThat(session.statusAt(OPENED_AT.plusSeconds(59))).isEqualTo(SessionStatus.OPEN);
    assertThat(session.statusAt(OPENED_AT.plusSeconds(60))).isEqualTo(SessionStatus.CLOSED);
  }

  @Test
  void rejectsNonPositiveWindow() {
    assertThatThrownBy(
            () -> new VotingSession(UUID.randomUUID(), agenda, OPENED_AT, OPENED_AT, 0, OPENED_AT))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
