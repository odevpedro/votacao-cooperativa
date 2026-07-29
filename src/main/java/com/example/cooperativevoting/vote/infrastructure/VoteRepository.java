package com.example.cooperativevoting.vote.infrastructure;

import com.example.cooperativevoting.vote.domain.Vote;
import com.example.cooperativevoting.vote.domain.VoteChoice;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoteRepository extends JpaRepository<Vote, UUID> {
  boolean existsBySessionIdAndAssociateId(UUID sessionId, String associateId);

  @Query(
      """
            select v.choice as choice, count(v) as total
            from Vote v
            where v.session.id = :sessionId
            group by v.choice
            """)
  List<ChoiceCount> countByChoice(@Param("sessionId") UUID sessionId);

  interface ChoiceCount {
    VoteChoice getChoice();

    long getTotal();
  }
}
