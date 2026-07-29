package com.example.cooperativevoting.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.cooperativevoting.agenda.application.AgendaService;
import com.example.cooperativevoting.shared.api.ApiRoutes;
import com.example.cooperativevoting.vote.application.DuplicateVoteException;
import com.example.cooperativevoting.vote.application.VoteService;
import com.example.cooperativevoting.vote.domain.VoteChoice;
import com.example.cooperativevoting.votingsession.application.VotingSessionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class CooperativeVotingIntegrationTest {
  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:17-alpine")
          .withDatabaseName("cooperative_voting")
          .withUsername("cooperative")
          .withPassword("cooperative");

  @DynamicPropertySource
  static void databaseProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired JdbcTemplate jdbc;
  @Autowired AgendaService agendaService;
  @Autowired VotingSessionService sessionService;
  @Autowired VoteService voteService;

  @BeforeEach
  void cleanDatabase() {
    jdbc.execute("TRUNCATE TABLE votes, voting_sessions, agendas");
  }

  @Test
  void runsDomainAndMobileApiFlow() throws Exception {
    UUID agendaId = createAgenda("Orçamento anual");

    mvc.perform(
            post(ApiRoutes.Agendas.SESSIONS, agendaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.requestedDurationSeconds").value(60))
        .andExpect(jsonPath("$.status").value("OPEN"));

    mvc.perform(get(ApiRoutes.Mobile.AGENDAS))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tipo").value("SELECAO"))
        .andExpect(
            jsonPath("$.itens[0].url")
                .value("https://test.example/api/v1/mobile/agendas/" + agendaId + "/identify"));

    mvc.perform(post(ApiRoutes.Mobile.IDENTIFY, agendaId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tipo").value("FORMULARIO"))
        .andExpect(jsonPath("$.itens[1].tipo").value("INPUT_TEXTO"));

    mvc.perform(
            post(ApiRoutes.Mobile.VOTE_OPTIONS, agendaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                        {"associateId":"12345678901"}
                                        """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tipo").value("SELECAO"))
        .andExpect(jsonPath("$.itens[0].body.choice").value("SIM"))
        .andExpect(jsonPath("$.itens[1].body.choice").value("NAO"));

    mvc.perform(
            post(ApiRoutes.Agendas.VOTES, agendaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                        {"associateId":"12345678901","choice":"SIM"}
                                        """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.tipo").value("FORMULARIO"))
        .andExpect(jsonPath("$.titulo").value("Voto registrado"));

    mvc.perform(get(ApiRoutes.Agendas.RESULTS, agendaId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.yesVotes").value(1))
        .andExpect(jsonPath("$.noVotes").value(0))
        .andExpect(jsonPath("$.outcome").value("IN_PROGRESS"));
  }

  @Test
  void returnsProblemDetailsForValidationAndDuplicateVote() throws Exception {
    UUID agendaId = createAgenda("Pauta");
    openSession(agendaId);

    mvc.perform(
            post(ApiRoutes.Agendas.VOTES, agendaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                        {"associateId":"123","choice":"SIM"}
                                        """))
        .andExpect(status().isBadRequest())
        .andExpect(header().exists("X-Correlation-Id"))
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.correlationId").isNotEmpty());

    vote(agendaId, "12345678901");
    mvc.perform(
            post(ApiRoutes.Agendas.VOTES, agendaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                        {"associateId":"12345678901","choice":"NAO"}
                                        """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("DUPLICATE_VOTE"))
        .andExpect(jsonPath("$.status").value(409));
  }

  @Test
  void supportsCustomDurationAndDatabaseConstraints() throws Exception {
    UUID agendaId = createAgenda("Pauta customizada");

    mvc.perform(
            post(ApiRoutes.Agendas.SESSIONS, agendaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"durationMinutes\":2}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.requestedDurationSeconds").value(120));

    mvc.perform(
            post(ApiRoutes.Agendas.SESSIONS, agendaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("SESSION_ALREADY_EXISTS"));

    Integer migrations =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM flyway_schema_history WHERE success = true", Integer.class);
    assertThat(migrations).isPositive();
  }

  @Test
  void acceptsExactlyOneOfConcurrentDuplicateVotes() throws Exception {
    var agenda = agendaService.create("Concorrência", null);
    sessionService.open(agenda.getId(), Duration.ofMinutes(5));
    int attempts = 8;
    var barrier = new CyclicBarrier(attempts);
    var executor = Executors.newFixedThreadPool(attempts);
    try {
      var tasks =
          IntStream.range(0, attempts)
              .mapToObj(
                  ignored ->
                      (Callable<Boolean>)
                          () -> {
                            barrier.await();
                            try {
                              voteService.register(agenda.getId(), "12345678901", VoteChoice.SIM);
                              return true;
                            } catch (DuplicateVoteException exception) {
                              return false;
                            }
                          })
              .toList();

      long accepted =
          executor.invokeAll(tasks).stream()
              .filter(
                  future -> {
                    try {
                      return future.get();
                    } catch (Exception exception) {
                      throw new AssertionError(exception);
                    }
                  })
              .count();

      assertThat(accepted).isEqualTo(1);
      assertThat(voteService.result(agenda.getId()).totalVotes()).isEqualTo(1);
    } finally {
      executor.shutdownNow();
    }
  }

  private UUID createAgenda(String title) throws Exception {
    var response =
        mvc.perform(
                post(ApiRoutes.Agendas.ROOT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            java.util.Map.of("title", title, "description", "Descrição"))))
            .andExpect(status().isCreated())
            .andExpect(
                header()
                    .string("Location", org.hamcrest.Matchers.containsString("/api/v1/agendas/")))
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode json = objectMapper.readTree(response);
    return UUID.fromString(json.get("id").asText());
  }

  private void openSession(UUID agendaId) throws Exception {
    mvc.perform(
            post(ApiRoutes.Agendas.SESSIONS, agendaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isCreated());
  }

  private void vote(UUID agendaId, String associateId) throws Exception {
    mvc.perform(
            post(ApiRoutes.Agendas.VOTES, agendaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        java.util.Map.of("associateId", associateId, "choice", "SIM"))))
        .andExpect(status().isCreated());
  }
}
