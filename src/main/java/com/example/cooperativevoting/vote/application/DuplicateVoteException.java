package com.example.cooperativevoting.vote.application;

public class DuplicateVoteException extends RuntimeException {
  public DuplicateVoteException() {
    super("O associado já votou nesta pauta.");
  }
}
