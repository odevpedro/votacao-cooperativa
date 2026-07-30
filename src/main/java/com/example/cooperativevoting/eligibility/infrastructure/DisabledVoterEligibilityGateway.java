package com.example.cooperativevoting.eligibility.infrastructure;

import com.example.cooperativevoting.eligibility.application.EligibilityResult;
import com.example.cooperativevoting.eligibility.application.VoterEligibilityGateway;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "integration.voter-eligibility.enabled",
    havingValue = "false",
    matchIfMissing = true)
public class DisabledVoterEligibilityGateway implements VoterEligibilityGateway {
  private static final Logger LOGGER = LoggerFactory.getLogger(DisabledVoterEligibilityGateway.class);

  @PostConstruct
  void logDisabled() {
    LOGGER.info("event=eligibility.disabled eligibilityCheck=bypassed");
  }

  @Override
  public EligibilityResult check(String cpf) {
    return EligibilityResult.ABLE_TO_VOTE;
  }
}
