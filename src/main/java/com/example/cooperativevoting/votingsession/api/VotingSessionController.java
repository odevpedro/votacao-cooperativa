package com.example.cooperativevoting.votingsession.api;

import static com.example.cooperativevoting.votingsession.api.VotingSessionApiModels.OpenSessionRequest;
import static com.example.cooperativevoting.votingsession.api.VotingSessionApiModels.VotingSessionResponse;

import com.example.cooperativevoting.shared.api.ApiRoutes;
import com.example.cooperativevoting.votingsession.application.VotingSessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@Tag(name = "Sessões")
public class VotingSessionController {
  private final VotingSessionService service;

  public VotingSessionController(VotingSessionService service) {
    this.service = service;
  }

  @PostMapping(ApiRoutes.Agendas.SESSIONS)
  ResponseEntity<VotingSessionResponse> open(
      @PathVariable UUID agendaId,
      @Valid @RequestBody(required = false) OpenSessionRequest request) {
    Duration duration =
        request == null || request.durationMinutes() == null
            ? null
            : Duration.ofMinutes(request.durationMinutes());
    var session = service.open(agendaId, duration);
    var response = VotingSessionResponse.from(session, service.status(session));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/current").build().toUri();
    return ResponseEntity.created(location).body(response);
  }

  @GetMapping(ApiRoutes.Agendas.CURRENT_SESSION)
  VotingSessionResponse current(@PathVariable UUID agendaId) {
    var session = service.getByAgenda(agendaId);
    return VotingSessionResponse.from(session, service.status(session));
  }
}
