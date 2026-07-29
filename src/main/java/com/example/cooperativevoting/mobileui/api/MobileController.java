package com.example.cooperativevoting.mobileui.api;

import static com.example.cooperativevoting.mobileui.api.MobileApiModels.VoteOptionsRequest;
import static com.example.cooperativevoting.mobileui.model.MobileScreenModels.FormScreen;
import static com.example.cooperativevoting.mobileui.model.MobileScreenModels.SelectionScreen;

import com.example.cooperativevoting.mobileui.application.MobileScreenService;
import com.example.cooperativevoting.shared.api.ApiRoutes;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Mobile UI")
public class MobileController {
  private final MobileScreenService service;

  public MobileController(MobileScreenService service) {
    this.service = service;
  }

  @GetMapping(ApiRoutes.Mobile.AGENDAS)
  SelectionScreen agendas() {
    return service.agendas();
  }

  @PostMapping(ApiRoutes.Mobile.IDENTIFY)
  FormScreen identify(@PathVariable UUID agendaId) {
    return service.identify(agendaId);
  }

  @PostMapping(ApiRoutes.Mobile.VOTE_OPTIONS)
  SelectionScreen voteOptions(
      @PathVariable UUID agendaId, @Valid @RequestBody VoteOptionsRequest request) {
    return service.voteOptions(agendaId, request.associateId());
  }
}
