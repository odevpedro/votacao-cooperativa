package com.example.cooperativevoting.mobileui.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record VoteOptionsRequest(
    UUID agendaId,
    @NotBlank @Pattern(regexp = "\\d{11}", message = "associateId deve conter 11 dígitos")
        String associateId) {}
