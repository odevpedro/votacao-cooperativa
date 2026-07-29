package com.example.cooperativevoting.vote.application;

import com.example.cooperativevoting.eligibility.application.VoterEligibilityService;
import com.example.cooperativevoting.vote.domain.Vote;
import com.example.cooperativevoting.vote.domain.VoteChoice;
import com.example.cooperativevoting.vote.infrastructure.VoteRepository;
import com.example.cooperativevoting.votingsession.application.VotingSessionService;
import com.example.cooperativevoting.votingsession.domain.SessionStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoteService {
  private static final Logger LOGGER = LoggerFactory.getLogger(VoteService.class);
  private final VoteRepository repository;
  private final VotingSessionService sessionService;
  private final VoterEligibilityService eligibilityService;
  private final Clock clock;

  public VoteService(
      VoteRepository repository,
      VotingSessionService sessionService,
      VoterEligibilityService eligibilityService,
      Clock clock) {
    this.repository = repository;
    this.sessionService = sessionService;
    this.eligibilityService = eligibilityService;
    this.clock = clock;
  }

  @Transactional
  public Vote register(UUID agendaId, String associateId, VoteChoice choice) {
    String normalizedAssociateId = normalizeAssociateId(associateId);
    var session = sessionService.requireOpen(agendaId);
    eligibilityService.requireEligible(normalizedAssociateId);
    if (repository.existsBySessionIdAndAssociateId(session.getId(), normalizedAssociateId)) {
      LOGGER.debug("event=vote.duplicate agendaId={} sessionId={}", agendaId, session.getId());
      throw new DuplicateVoteException();
    }
    var vote =
        new Vote(UUID.randomUUID(), session, normalizedAssociateId, choice, Instant.now(clock));
    try {
      var created = repository.saveAndFlush(vote);
      LOGGER.debug(
          "event=vote.accepted agendaId={} sessionId={} choice={}",
          agendaId,
          session.getId(),
          choice);
      return created;
    } catch (DataIntegrityViolationException exception) {
      LOGGER.debug("event=vote.duplicate agendaId={} sessionId={}", agendaId, session.getId());
      throw new DuplicateVoteException();
    }
  }

  @Transactional(readOnly = true)
  public VotingResult result(UUID agendaId) {
    var session = sessionService.getByAgenda(agendaId);
    long yes = 0;
    long no = 0;
    for (var count : repository.countByChoice(session.getId())) {
      if (count.getChoice() == VoteChoice.SIM) {
        yes = count.getTotal();
      } else {
        no = count.getTotal();
      }
    }
    SessionStatus status = sessionService.status(session);
    VoteOutcome outcome =
        status == SessionStatus.CLOSED ? finalOutcome(yes, no) : VoteOutcome.IN_PROGRESS;
    return new VotingResult(agendaId, session.getId(), status, yes, no, yes + no, outcome);
  }

  private VoteOutcome finalOutcome(long yes, long no) {
    if (yes == no) {
      return VoteOutcome.TIED;
    }
    return yes > no ? VoteOutcome.APPROVED : VoteOutcome.REJECTED;
  }

  private String normalizeAssociateId(String associateId) {
    if (associateId == null || associateId.isBlank() || associateId.trim().length() > 64) {
      throw new IllegalArgumentException(
          "O identificador do associado deve conter de 1 a 64 caracteres.");
    }
    return associateId.trim();
  }
}
