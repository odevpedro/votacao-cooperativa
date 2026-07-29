package com.example.cooperativevoting.vote.api;

import com.example.cooperativevoting.shared.api.ApiRoutes;
import com.example.cooperativevoting.vote.application.VoteService;
import com.example.cooperativevoting.vote.application.VotingResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Resultados")
public class ResultController {
  private final VoteService service;

  public ResultController(VoteService service) {
    this.service = service;
  }

  @GetMapping(ApiRoutes.Agendas.RESULTS)
  VotingResult result(@PathVariable UUID agendaId) {
    return service.result(agendaId);
  }
}
