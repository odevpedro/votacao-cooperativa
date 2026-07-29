package com.example.cooperativevoting.mobileui.model;

import java.util.List;

public record SelectionScreen(String tipo, String titulo, List<SelectionItem> itens) {
  public SelectionScreen(String titulo, List<SelectionItem> itens) {
    this("SELECAO", titulo, itens);
  }
}
