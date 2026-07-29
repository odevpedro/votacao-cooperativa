package com.example.cooperativevoting.eligibility.application;

public class VoterNotEligibleException extends RuntimeException {
  public VoterNotEligibleException() {
    super("O associado não está habilitado para votar.");
  }
}
