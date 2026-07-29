package com.example.cooperativevoting.mobileui.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

public final class MobileScreenModels {
  private MobileScreenModels() {}

  public record SelectionScreen(String tipo, String titulo, List<SelectionItem> itens) {
    public SelectionScreen(String titulo, List<SelectionItem> itens) {
      this("SELECAO", titulo, itens);
    }
  }

  public record SelectionItem(String texto, String url, Map<String, Object> body) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record FormScreen(
      String tipo, String titulo, List<FormItem> itens, Action botaoOk, Action botaoCancelar) {
    public FormScreen(String titulo, List<FormItem> itens, Action botaoOk, Action botaoCancelar) {
      this("FORMULARIO", titulo, itens, botaoOk, botaoCancelar);
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record FormItem(String tipo, String texto, String id, String titulo, String valor) {
    public static FormItem text(String text) {
      return new FormItem("TEXTO", text, null, null, null);
    }

    public static FormItem inputText(String id, String title) {
      return new FormItem("INPUT_TEXTO", null, id, title, "");
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record Action(String texto, String url, Map<String, Object> body) {}
}
