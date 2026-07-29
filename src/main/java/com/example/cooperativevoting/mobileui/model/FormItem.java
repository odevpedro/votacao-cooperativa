package com.example.cooperativevoting.mobileui.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FormItem(String tipo, String texto, String id, String titulo, String valor) {
  public static FormItem text(String text) {
    return new FormItem("TEXTO", text, null, null, null);
  }

  public static FormItem inputText(String id, String title) {
    return new FormItem("INPUT_TEXTO", null, id, title, "");
  }
}
