package com.example.cooperativevoting.eligibility.infrastructure;

import com.example.cooperativevoting.eligibility.application.EligibilityResult;
import com.example.cooperativevoting.eligibility.application.VoterEligibilityGateway;
import java.net.http.HttpClient;
import java.util.concurrent.CancellationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@ConditionalOnProperty(
    name = "integration.voter-eligibility.enabled",
    havingValue = "true",
    matchIfMissing = false)
public class HttpVoterEligibilityGateway implements VoterEligibilityGateway {
  private static final Logger LOGGER = LoggerFactory.getLogger(HttpVoterEligibilityGateway.class);
  private final RestClient restClient;

  public HttpVoterEligibilityGateway(EligibilityProperties properties) {
    var httpClient = HttpClient.newBuilder().connectTimeout(properties.connectTimeout()).build();
    var requestFactory = new JdkClientHttpRequestFactory(httpClient);
    requestFactory.setReadTimeout(properties.readTimeout());
    this.restClient =
        RestClient.builder()
            .baseUrl(stripTrailingSlash(properties.baseUrl()))
            .requestFactory(requestFactory)
            .build();
  }

  @Override
  public EligibilityResult check(String cpf) {
    if (cpf == null || !cpf.matches("\\d{11}")) {
      return EligibilityResult.INVALID_CPF;
    }
    long start = System.nanoTime();
    try {
      var response =
          restClient.get().uri("/users/{cpf}", cpf).retrieve().body(EligibilityResponse.class);
      long elapsed = (System.nanoTime() - start) / 1_000_000;
      LOGGER.debug("event=eligibility.success cpf={} elapsedMs={} status={}", cpf, elapsed, response);
      if (response == null || response.status() == null) {
        return unavailable("invalid-response", null);
      }
      try {
        return EligibilityResult.valueOf(response.status());
      } catch (IllegalArgumentException exception) {
        return unavailable("unknown-status", exception);
      }
    } catch (HttpClientErrorException.NotFound exception) {
      long elapsed = (System.nanoTime() - start) / 1_000_000;
      LOGGER.debug("event=eligibility.not-found cpf={} elapsedMs={}", cpf, elapsed);
      return isUpstreamApplicationMissing(exception)
          ? unavailable("upstream-application-not-found", exception)
          : EligibilityResult.INVALID_CPF;
    } catch (HttpClientErrorException exception) {
      if (exception.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
        return EligibilityResult.INVALID_CPF;
      }
      return unavailable("http-error", exception);
    } catch (RestClientException | CancellationException exception) {
      return unavailable("connection-error", exception);
    }
  }

  private boolean isUpstreamApplicationMissing(HttpClientErrorException.NotFound exception) {
    MediaType contentType = exception.getResponseHeaders().getContentType();
    String body = exception.getResponseBodyAsString();
    return (contentType != null && MediaType.TEXT_HTML.isCompatibleWith(contentType))
        || body.contains("No such app")
        || body.contains("no-such-app");
  }

  private EligibilityResult unavailable(String reason, Exception exception) {
    if (exception != null) {
      LOGGER.warn("event=eligibility.unavailable reason={}", reason, exception);
    } else {
      LOGGER.warn("event=eligibility.unavailable reason={}", reason);
    }
    return EligibilityResult.SERVICE_UNAVAILABLE;
  }

  private String stripTrailingSlash(String value) {
    return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
  }

  private record EligibilityResponse(String status) {}
}
