package com.example.cooperativevoting.votingsession.infrastructure;

import com.example.cooperativevoting.votingsession.domain.VotingSession;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotingSessionRepository extends JpaRepository<VotingSession, UUID> {
  Optional<VotingSession> findByAgendaId(UUID agendaId);

  boolean existsByAgendaId(UUID agendaId);
}
