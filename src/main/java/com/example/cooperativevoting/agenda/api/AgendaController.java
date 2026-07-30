package com.example.cooperativevoting.agenda.api;

import com.example.cooperativevoting.agenda.application.AgendaService;
import com.example.cooperativevoting.shared.api.ApiRoutes;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@Tag(name = "Pautas")
public class AgendaController {
  private static final Logger LOGGER = LoggerFactory.getLogger(AgendaController.class);
  private final AgendaService service;

  public AgendaController(AgendaService service) {
    this.service = service;
  }

  @PostMapping(ApiRoutes.Agendas.ROOT)
  ResponseEntity<AgendaResponse> create(@Valid @RequestBody CreateAgendaRequest request) {
    LOGGER.debug("event=agenda.create-request");
    var response = AgendaResponse.from(service.create(request.title(), request.description()));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();
    return ResponseEntity.created(location).body(response);
  }

  @GetMapping(ApiRoutes.Agendas.ROOT)
  List<AgendaResponse> list() {
    LOGGER.debug("event=agenda.list-request");
    return service.list().stream().map(AgendaResponse::from).toList();
  }

  @GetMapping(ApiRoutes.Agendas.BY_ID)
  AgendaResponse get(@PathVariable UUID agendaId) {
    LOGGER.debug("event=agenda.get-request agendaId={}", agendaId);
    return AgendaResponse.from(service.get(agendaId));
  }
}
