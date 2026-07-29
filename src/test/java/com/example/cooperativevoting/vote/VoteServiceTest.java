package com.example.cooperativevoting.vote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.cooperativevoting.agenda.domain.Agenda;
import com.example.cooperativevoting.eligibility.application.VoterEligibilityService;
import com.example.cooperativevoting.vote.application.VoteOutcome;
import com.example.cooperativevoting.vote.application.VoteService;
import com.example.cooperativevoting.vote.domain.VoteChoice;
import com.example.cooperativevoting.vote.infrastructure.VoteRepository;
import com.example.cooperativevoting.votingsession.application.VotingSessionService;
import com.example.cooperativevoting.votingsession.domain.SessionStatus;
import com.example.cooperativevoting.votingsession.domain.VotingSession;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VoteServiceTest {
  private final VoteRepository repository = mock(VoteRepository.class);
  private final VotingSessionService sessionService = mock(VotingSessionService.class);
  private final UUID agendaId = UUID.randomUUID();
  private final VotingSession session = session();
  private final VoteService service =
      new VoteService(
          repository, sessionService, mock(VoterEligibilityService.class), Clock.systemUTC());

  @BeforeEach
  void setUp() {
    when(sessionService.getByAgenda(agendaId)).thenReturn(session);
  }

  @Test
  void calculatesApprovedRejectedAndTiedAfterClosing() {
    when(sessionService.status(session)).thenReturn(SessionStatus.CLOSED);

    counts(2, 1);
    assertThat(service.result(agendaId).outcome()).isEqualTo(VoteOutcome.APPROVED);

    counts(1, 2);
    assertThat(service.result(agendaId).outcome()).isEqualTo(VoteOutcome.REJECTED);

    counts(3, 3);
    assertThat(service.result(agendaId).outcome()).isEqualTo(VoteOutcome.TIED);
  }

  @Test
  void returnsPartialCountsWhileSessionIsOpen() {
    when(sessionService.status(session)).thenReturn(SessionStatus.OPEN);
    counts(4, 2);

    var result = service.result(agendaId);

    assertThat(result.outcome()).isEqualTo(VoteOutcome.IN_PROGRESS);
    assertThat(result.totalVotes()).isEqualTo(6);
  }

  private void counts(long yes, long no) {
    var yesCount = mock(VoteRepository.ChoiceCount.class);
    var noCount = mock(VoteRepository.ChoiceCount.class);
    when(yesCount.getChoice()).thenReturn(VoteChoice.SIM);
    when(yesCount.getTotal()).thenReturn(yes);
    when(noCount.getChoice()).thenReturn(VoteChoice.NAO);
    when(noCount.getTotal()).thenReturn(no);
    when(repository.countByChoice(session.getId())).thenReturn(List.of(yesCount, noCount));
  }

  private VotingSession session() {
    Instant now = Instant.parse("2026-07-28T16:00:00Z");
    var agenda = new Agenda(agendaId, "Pauta", null, now);
    return new VotingSession(UUID.randomUUID(), agenda, now, now.plusSeconds(60), 60, now);
  }
}
