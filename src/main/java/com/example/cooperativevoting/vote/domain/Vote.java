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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "votes")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
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
}
