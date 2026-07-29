package com.example.cooperativevoting.mobileui.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public final class MobileApiModels {
  private MobileApiModels() {}

  public record IdentifyRequest(UUID agendaId) {}

  public record VoteOptionsRequest(
      UUID agendaId,
      @NotBlank @Pattern(regexp = "\\d{11}", message = "associateId deve conter 11 dígitos")
          String associateId) {}
}
