package com.example.cooperativevoting.eligibility.application;

public class EligibilityServiceUnavailableException extends RuntimeException {
  public EligibilityServiceUnavailableException() {
    super("Não foi possível verificar a elegibilidade do associado.");
  }
}
