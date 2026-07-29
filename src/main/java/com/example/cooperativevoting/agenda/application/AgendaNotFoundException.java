package com.example.cooperativevoting.agenda.application;

import java.util.UUID;

public class AgendaNotFoundException extends RuntimeException {
  public AgendaNotFoundException(UUID agendaId) {
    super("Pauta %s não encontrada.".formatted(agendaId));
  }
}
