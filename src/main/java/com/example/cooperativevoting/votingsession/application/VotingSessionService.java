package com.example.cooperativevoting.votingsession.application;

import com.example.cooperativevoting.agenda.application.AgendaService;
import com.example.cooperativevoting.votingsession.domain.SessionStatus;
import com.example.cooperativevoting.votingsession.domain.VotingSession;
import com.example.cooperativevoting.votingsession.infrastructure.VotingSessionRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VotingSessionService {
  public static final Duration DEFAULT_DURATION = Duration.ofMinutes(1);
  private static final Logger LOGGER = LoggerFactory.getLogger(VotingSessionService.class);
  private final VotingSessionRepository repository;
  private final AgendaService agendaService;
  private final Clock clock;

  public VotingSessionService(
      VotingSessionRepository repository, AgendaService agendaService, Clock clock) {
    this.repository = repository;
    this.agendaService = agendaService;
    this.clock = clock;
  }

  @Transactional
  public VotingSession open(UUID agendaId, Duration requestedDuration) {
    Duration duration = requestedDuration == null ? DEFAULT_DURATION : requestedDuration;
    if (duration.isZero() || duration.isNegative()) {
      throw new IllegalArgumentException("A duração da sessão deve ser positiva.");
    }
    if (repository.existsByAgendaId(agendaId)) {
      throw new SessionAlreadyExistsException();
    }
    var agenda = agendaService.get(agendaId);
    Instant now = Instant.now(clock);
    var session =
        new VotingSession(
            UUID.randomUUID(), agenda, now, now.plus(duration), duration.toSeconds(), now);
    try {
      var created = repository.saveAndFlush(session);
      LOGGER.info(
          "event=session.opened agendaId={} sessionId={} durationSeconds={}",
          agendaId,
          created.getId(),
          duration.toSeconds());
      return created;
    } catch (DataIntegrityViolationException exception) {
      throw new SessionAlreadyExistsException();
    }
  }

  @Transactional(readOnly = true)
  public VotingSession getByAgenda(UUID agendaId) {
    agendaService.get(agendaId);
    return repository.findByAgendaId(agendaId).orElseThrow(SessionNotFoundException::new);
  }

  @Transactional(readOnly = true)
  public VotingSession requireOpen(UUID agendaId) {
    var session = getByAgenda(agendaId);
    SessionStatus status = session.statusAt(Instant.now(clock));
    if (status != SessionStatus.OPEN) {
      throw new SessionNotOpenException(status);
    }
    return session;
  }

  public SessionStatus status(VotingSession session) {
    return session.statusAt(Instant.now(clock));
  }
}
