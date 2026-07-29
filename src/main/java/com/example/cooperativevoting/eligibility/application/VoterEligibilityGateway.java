package com.example.cooperativevoting.eligibility.application;

public interface VoterEligibilityGateway {
  EligibilityResult check(String cpf);
}
