package com.example.cooperativevoting.votingsession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.cooperativevoting.agenda.application.AgendaService;
import com.example.cooperativevoting.agenda.domain.Agenda;
import com.example.cooperativevoting.votingsession.application.VotingSessionService;
import com.example.cooperativevoting.votingsession.domain.VotingSession;
import com.example.cooperativevoting.votingsession.infrastructure.VotingSessionRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VotingSessionServiceTest {
  private static final Instant NOW = Instant.parse("2026-07-28T16:00:00Z");
  private final VotingSessionRepository repository = mock(VotingSessionRepository.class);
  private final AgendaService agendaService = mock(AgendaService.class);
  private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
  private final UUID agendaId = UUID.randomUUID();
  private final VotingSessionService service =
      new VotingSessionService(repository, agendaService, clock);

  @BeforeEach
  void setUp() {
    when(agendaService.get(agendaId)).thenReturn(new Agenda(agendaId, "Pauta", null, NOW));
    when(repository.saveAndFlush(any(VotingSession.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void defaultsToOneMinute() {
    var session = service.open(agendaId, null);

    assertThat(session.getOpenedAt()).isEqualTo(NOW);
    assertThat(session.getClosesAt()).isEqualTo(NOW.plusSeconds(60));
    assertThat(session.getRequestedDurationSeconds()).isEqualTo(60);
  }

  @Test
  void acceptsCustomDuration() {
    var session = service.open(agendaId, Duration.ofMinutes(7));
    assertThat(session.getClosesAt()).isEqualTo(NOW.plusSeconds(420));
  }

  @Test
  void rejectsInvalidDuration() {
    assertThatThrownBy(() -> service.open(agendaId, Duration.ZERO))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
