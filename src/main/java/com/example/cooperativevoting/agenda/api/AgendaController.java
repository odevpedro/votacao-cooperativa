package com.example.cooperativevoting.agenda.api;

import static com.example.cooperativevoting.agenda.api.AgendaApiModels.AgendaResponse;
import static com.example.cooperativevoting.agenda.api.AgendaApiModels.CreateAgendaRequest;

import com.example.cooperativevoting.agenda.application.AgendaService;
import com.example.cooperativevoting.shared.api.ApiRoutes;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
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
  private final AgendaService service;

  public AgendaController(AgendaService service) {
    this.service = service;
  }

  @PostMapping(ApiRoutes.Agendas.ROOT)
  ResponseEntity<AgendaResponse> create(@Valid @RequestBody CreateAgendaRequest request) {
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
    return service.list().stream().map(AgendaResponse::from).toList();
  }

  @GetMapping(ApiRoutes.Agendas.BY_ID)
  AgendaResponse get(@PathVariable UUID agendaId) {
    return AgendaResponse.from(service.get(agendaId));
  }
}
