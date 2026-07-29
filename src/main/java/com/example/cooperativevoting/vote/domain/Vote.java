package com.example.cooperativevoting.vote.domain;

import com.example.cooperativevoting.votingsession.domain.VotingSession;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "votes")
public class Vote {
  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "session_id", nullable = false)
  private VotingSession session;

  @Column(name = "associate_id", nullable = false, length = 64)
  private String associateId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 3)
  private VoteChoice choice;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected Vote() {}

  public Vote(
      UUID id, VotingSession session, String associateId, VoteChoice choice, Instant createdAt) {
    this.id = id;
    this.session = session;
    this.associateId = associateId;
    this.choice = choice;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public VotingSession getSession() {
    return session;
  }

  public VoteChoice getChoice() {
    return choice;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
