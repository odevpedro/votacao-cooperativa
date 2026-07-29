package com.example.cooperativevoting.eligibility;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.cooperativevoting.eligibility.application.EligibilityResult;
import com.example.cooperativevoting.eligibility.infrastructure.EligibilityProperties;
import com.example.cooperativevoting.eligibility.infrastructure.HttpVoterEligibilityGateway;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HttpVoterEligibilityGatewayTest {
  private WireMockServer server;
  private HttpVoterEligibilityGateway gateway;

  @BeforeEach
  void setUp() {
    server = new WireMockServer(0);
    server.start();
    gateway =
        new HttpVoterEligibilityGateway(
            new EligibilityProperties(
                true, server.baseUrl(), Duration.ofSeconds(1), Duration.ofSeconds(2)));
  }

  @AfterEach
  void tearDown() {
    server.stop();
  }

  @Test
  void mapsAbleUnableAndNotFound() {
    stub(200, "{\"status\":\"ABLE_TO_VOTE\"}");
    assertThat(gateway.check("12345678901")).isEqualTo(EligibilityResult.ABLE_TO_VOTE);

    stub(200, "{\"status\":\"UNABLE_TO_VOTE\"}");
    assertThat(gateway.check("12345678901")).isEqualTo(EligibilityResult.UNABLE_TO_VOTE);

    stub(404, "");
    assertThat(gateway.check("12345678901")).isEqualTo(EligibilityResult.INVALID_CPF);
  }

  @Test
  void mapsServerErrorsInvalidResponsesAndTimeoutsToUnavailable() {
    stub(500, "");
    assertThat(gateway.check("12345678901")).isEqualTo(EligibilityResult.SERVICE_UNAVAILABLE);

    stub(200, "{\"unexpected\":true}");
    assertThat(gateway.check("12345678901")).isEqualTo(EligibilityResult.SERVICE_UNAVAILABLE);

    gateway =
        new HttpVoterEligibilityGateway(
            new EligibilityProperties(
                true, server.baseUrl(), Duration.ofMillis(200), Duration.ofMillis(100)));
    server.resetAll();
    server.stubFor(
        get(urlEqualTo("/users/12345678901"))
            .willReturn(
                aResponse()
                    .withFixedDelay(300)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"status\":\"ABLE_TO_VOTE\"}")));
    assertThat(gateway.check("12345678901")).isEqualTo(EligibilityResult.SERVICE_UNAVAILABLE);
  }

  private void stub(int status, String body) {
    server.resetAll();
    server.stubFor(
        get(urlEqualTo("/users/12345678901"))
            .willReturn(
                aResponse()
                    .withStatus(status)
                    .withHeader("Content-Type", "application/json")
                    .withBody(body)));
  }
}
