package com.example.cooperativevoting.eligibility.application;

import org.springframework.stereotype.Service;

@Service
public class VoterEligibilityService {
  private final VoterEligibilityGateway gateway;

  public VoterEligibilityService(VoterEligibilityGateway gateway) {
    this.gateway = gateway;
  }

  public void requireEligible(String cpf) {
    if (cpf == null || !cpf.matches("\\d{11}")) {
      throw new InvalidCpfException();
    }
    switch (gateway.check(cpf)) {
      case ABLE_TO_VOTE -> {
        return;
      }
      case UNABLE_TO_VOTE -> throw new VoterNotEligibleException();
      case INVALID_CPF -> throw new InvalidCpfException();
      case SERVICE_UNAVAILABLE -> throw new EligibilityServiceUnavailableException();
    }
  }
}
