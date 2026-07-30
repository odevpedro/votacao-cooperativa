package com.example.cooperativevoting.agenda.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAgendaRequest(
    @NotBlank @Size(max = 150) String title, @Size(max = 2000) String description) {}
