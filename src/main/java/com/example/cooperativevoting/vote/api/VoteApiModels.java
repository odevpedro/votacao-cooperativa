package com.example.cooperativevoting.vote.api;

import com.example.cooperativevoting.vote.domain.VoteChoice;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public final class VoteApiModels {
  private VoteApiModels() {}

  public record RegisterVoteRequest(
      @NotBlank @Pattern(regexp = "\\d{11}", message = "associateId deve conter 11 dígitos")
          String associateId,
      @NotNull VoteChoice choice) {}
}
