package com.example.cooperativevoting.mobileui.api;

import com.example.cooperativevoting.mobileui.application.MobileScreenService;
import com.example.cooperativevoting.mobileui.model.FormScreen;
import com.example.cooperativevoting.mobileui.model.SelectionScreen;
import com.example.cooperativevoting.shared.api.ApiRoutes;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Mobile UI")
public class MobileController {
  private static final Logger LOGGER = LoggerFactory.getLogger(MobileController.class);
  private final MobileScreenService service;

  public MobileController(MobileScreenService service) {
    this.service = service;
  }

  @GetMapping(ApiRoutes.Mobile.AGENDAS)
  SelectionScreen agendas() {
    LOGGER.debug("event=mobile.agendas-request");
    return service.agendas();
  }

  @PostMapping(ApiRoutes.Mobile.IDENTIFY)
  FormScreen identify(@PathVariable UUID agendaId) {
    LOGGER.debug("event=mobile.identify-request agendaId={}", agendaId);
    return service.identify(agendaId);
  }

  @PostMapping(ApiRoutes.Mobile.VOTE_OPTIONS)
  SelectionScreen voteOptions(
      @PathVariable UUID agendaId, @Valid @RequestBody VoteOptionsRequest request) {
    LOGGER.debug("event=mobile.vote-options-request agendaId={}", agendaId);
    return service.voteOptions(agendaId, request.associateId());
  }
}
