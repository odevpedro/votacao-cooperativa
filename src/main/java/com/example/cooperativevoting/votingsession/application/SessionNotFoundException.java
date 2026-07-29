package com.example.cooperativevoting.votingsession.application;

public class SessionNotFoundException extends RuntimeException {
  public SessionNotFoundException() {
    super("A pauta ainda não possui uma sessão de votação.");
  }
}
