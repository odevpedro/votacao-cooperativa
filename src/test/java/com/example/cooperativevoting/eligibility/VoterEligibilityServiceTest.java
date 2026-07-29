package com.example.cooperativevoting.eligibility;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.cooperativevoting.eligibility.application.EligibilityResult;
import com.example.cooperativevoting.eligibility.application.EligibilityServiceUnavailableException;
import com.example.cooperativevoting.eligibility.application.InvalidCpfException;
import com.example.cooperativevoting.eligibility.application.VoterEligibilityGateway;
import com.example.cooperativevoting.eligibility.application.VoterEligibilityService;
import com.example.cooperativevoting.eligibility.application.VoterNotEligibleException;
import org.junit.jupiter.api.Test;

class VoterEligibilityServiceTest {
  private static final String CPF = "12345678901";
  private final VoterEligibilityGateway gateway = mock(VoterEligibilityGateway.class);
  private final VoterEligibilityService service = new VoterEligibilityService(gateway);

  @Test
  void acceptsEligibleAssociate() {
    when(gateway.check(CPF)).thenReturn(EligibilityResult.ABLE_TO_VOTE);
    assertThatCode(() -> service.requireEligible(CPF)).doesNotThrowAnyException();
  }

  @Test
  void mapsAllRejections() {
    when(gateway.check(CPF)).thenReturn(EligibilityResult.UNABLE_TO_VOTE);
    assertThatThrownBy(() -> service.requireEligible(CPF))
        .isInstanceOf(VoterNotEligibleException.class);

    when(gateway.check(CPF)).thenReturn(EligibilityResult.INVALID_CPF);
    assertThatThrownBy(() -> service.requireEligible(CPF)).isInstanceOf(InvalidCpfException.class);

    when(gateway.check(CPF)).thenReturn(EligibilityResult.SERVICE_UNAVAILABLE);
    assertThatThrownBy(() -> service.requireEligible(CPF))
        .isInstanceOf(EligibilityServiceUnavailableException.class);
  }

  @Test
  void acceptsGenericIdentifierWhenGatewayAllowsIt() {
    when(gateway.check("member-42")).thenReturn(EligibilityResult.ABLE_TO_VOTE);

    assertThatCode(() -> service.requireEligible("member-42")).doesNotThrowAnyException();
  }
}
