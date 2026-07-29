package com.example.cooperativevoting.mobileui.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FormScreen(
    String tipo, String titulo, List<FormItem> itens, Action botaoOk, Action botaoCancelar) {
  public FormScreen(String titulo, List<FormItem> itens, Action botaoOk, Action botaoCancelar) {
    this("FORMULARIO", titulo, itens, botaoOk, botaoCancelar);
  }
}
