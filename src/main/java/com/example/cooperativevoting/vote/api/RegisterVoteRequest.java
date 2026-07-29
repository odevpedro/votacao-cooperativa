package com.example.cooperativevoting.vote.api;

import com.example.cooperativevoting.vote.domain.VoteChoice;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterVoteRequest(
    @NotBlank @Size(min = 1, max = 64) String associateId, @NotNull VoteChoice choice) {}
