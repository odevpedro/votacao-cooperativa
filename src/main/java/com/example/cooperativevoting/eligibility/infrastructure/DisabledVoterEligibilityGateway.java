package com.example.cooperativevoting.eligibility.infrastructure;

import com.example.cooperativevoting.eligibility.application.EligibilityResult;
import com.example.cooperativevoting.eligibility.application.VoterEligibilityGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "integration.voter-eligibility.enabled",
    havingValue = "false",
    matchIfMissing = false)
public class DisabledVoterEligibilityGateway implements VoterEligibilityGateway {
  @Override
  public EligibilityResult check(String cpf) {
    return EligibilityResult.ABLE_TO_VOTE;
  }
}
