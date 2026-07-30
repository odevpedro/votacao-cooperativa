package com.example.cooperativevoting.eligibility.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VoterEligibilityService {
  private static final Logger LOGGER = LoggerFactory.getLogger(VoterEligibilityService.class);
  private final VoterEligibilityGateway gateway;

  public VoterEligibilityService(VoterEligibilityGateway gateway) {
    this.gateway = gateway;
  }

  public void requireEligible(String associateId) {
    var result = gateway.check(associateId);
    LOGGER.debug("event=eligibility.check result={}", result);
    switch (result) {
      case ABLE_TO_VOTE -> {
        return;
      }
      case UNABLE_TO_VOTE -> throw new VoterNotEligibleException();
      case INVALID_CPF -> throw new InvalidCpfException();
      case SERVICE_UNAVAILABLE -> throw new EligibilityServiceUnavailableException();
    }
  }
}
