package com.example.cooperativevoting.votingsession.application;

public class SessionAlreadyExistsException extends RuntimeException {
  public SessionAlreadyExistsException() {
    super("A pauta já possui uma sessão de votação.");
  }
}
