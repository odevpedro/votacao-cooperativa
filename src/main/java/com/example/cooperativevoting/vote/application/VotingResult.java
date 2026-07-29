package com.example.cooperativevoting.vote.application;

import com.example.cooperativevoting.votingsession.domain.SessionStatus;
import java.util.UUID;

public record VotingResult(
    UUID agendaId,
    UUID sessionId,
    SessionStatus sessionStatus,
    long yesVotes,
    long noVotes,
    long totalVotes,
    VoteOutcome outcome) {}
