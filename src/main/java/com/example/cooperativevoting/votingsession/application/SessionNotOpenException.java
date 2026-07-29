package com.example.cooperativevoting.votingsession.application;

import com.example.cooperativevoting.votingsession.domain.SessionStatus;

public class SessionNotOpenException extends RuntimeException {
  private final SessionStatus status;

  public SessionNotOpenException(SessionStatus status) {
    super(
        status == SessionStatus.CLOSED
            ? "A sessão de votação está encerrada."
            : "A sessão de votação ainda não começou.");
    this.status = status;
  }

  public SessionStatus getStatus() {
    return status;
  }
}
