package com.example.cooperativevoting.mobileui.application;

import com.example.cooperativevoting.agenda.application.AgendaService;
import com.example.cooperativevoting.mobileui.model.Action;
import com.example.cooperativevoting.mobileui.model.FormItem;
import com.example.cooperativevoting.mobileui.model.FormScreen;
import com.example.cooperativevoting.mobileui.model.SelectionItem;
import com.example.cooperativevoting.mobileui.model.SelectionScreen;
import com.example.cooperativevoting.eligibility.application.VoterEligibilityService;
import com.example.cooperativevoting.shared.api.ApiRoutes;
import com.example.cooperativevoting.vote.domain.VoteChoice;
import com.example.cooperativevoting.votingsession.application.VotingSessionService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MobileScreenService {
  private static final Logger LOGGER = LoggerFactory.getLogger(MobileScreenService.class);
  private final AgendaService agendaService;
  private final VotingSessionService sessionService;
  private final VoterEligibilityService eligibilityService;
  private final PublicUrlBuilder urls;

  public MobileScreenService(
      AgendaService agendaService,
      VotingSessionService sessionService,
      VoterEligibilityService eligibilityService,
      PublicUrlBuilder urls) {
    this.agendaService = agendaService;
    this.sessionService = sessionService;
    this.eligibilityService = eligibilityService;
    this.urls = urls;
  }

  public SelectionScreen agendas() {
    LOGGER.debug("event=mobile.agendas-build");
    var items =
        agendaService.list().stream()
            .map(
                agenda ->
                    new SelectionItem(
                        agenda.getTitle(),
                        urls.path(ApiRoutes.Mobile.IDENTIFY, agenda.getId()),
                        Map.of("agendaId", agenda.getId().toString())))
            .toList();
    return new SelectionScreen("Pautas disponíveis", items);
  }

  public FormScreen identify(UUID agendaId) {
    LOGGER.debug("event=mobile.identify-build agendaId={}", agendaId);
    agendaService.get(agendaId);
    return new FormScreen(
        "Identificação do associado",
        List.of(
            FormItem.text("Informe seu CPF para continuar."),
            FormItem.inputText("associateId", "CPF")),
        new Action(
            "Continuar",
            urls.path(ApiRoutes.Mobile.VOTE_OPTIONS, agendaId),
            Map.of("agendaId", agendaId.toString())),
        new Action("Cancelar", urls.path(ApiRoutes.Mobile.AGENDAS), null));
  }

  public SelectionScreen voteOptions(UUID agendaId, String associateId) {
    LOGGER.debug("event=mobile.vote-options-build agendaId={}", agendaId);
    agendaService.get(agendaId);
    sessionService.requireOpen(agendaId);
    eligibilityService.requireEligible(associateId);
    String voteUrl = urls.path(ApiRoutes.Agendas.VOTES, agendaId);
    return new SelectionScreen(
        "Escolha seu voto",
        List.of(
            voteItem("Sim", voteUrl, associateId, VoteChoice.SIM),
            voteItem("Não", voteUrl, associateId, VoteChoice.NAO)));
  }

  public FormScreen confirmation(UUID agendaId) {
    var agenda = agendaService.get(agendaId);
    LOGGER.debug("event=mobile.confirmation-build agendaId={}", agendaId);
    return new FormScreen(
        "Voto registrado",
        List.of(
            FormItem.text("Seu voto foi registrado com sucesso."),
            FormItem.text("Pauta: " + agenda.getTitle())),
        new Action("Voltar às pautas", urls.path(ApiRoutes.Mobile.AGENDAS), null),
        null);
  }

  private SelectionItem voteItem(String text, String url, String associateId, VoteChoice choice) {
    return new SelectionItem(
        text, url, Map.of("associateId", associateId, "choice", choice.name()));
  }
}
