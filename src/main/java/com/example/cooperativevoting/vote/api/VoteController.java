package com.example.cooperativevoting.vote.api;

import com.example.cooperativevoting.mobileui.application.MobileScreenService;
import com.example.cooperativevoting.mobileui.model.FormScreen;
import com.example.cooperativevoting.shared.api.ApiRoutes;
import com.example.cooperativevoting.vote.application.VoteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Votos")
public class VoteController {
  private static final Logger LOGGER = LoggerFactory.getLogger(VoteController.class);
  private final VoteService service;
  private final MobileScreenService mobileScreenService;

  public VoteController(VoteService service, MobileScreenService mobileScreenService) {
    this.service = service;
    this.mobileScreenService = mobileScreenService;
  }

  @PostMapping(ApiRoutes.Agendas.VOTES)
  ResponseEntity<FormScreen> register(
      @PathVariable UUID agendaId, @Valid @RequestBody RegisterVoteRequest request) {
    LOGGER.debug("event=vote.register-request agendaId={} choice={}", agendaId, request.choice());
    service.register(agendaId, request.associateId(), request.choice());
    URI location = URI.create(ApiRoutes.Agendas.RESULTS.replace("{agendaId}", agendaId.toString()));
    return ResponseEntity.created(location).body(mobileScreenService.confirmation(agendaId));
  }
}
