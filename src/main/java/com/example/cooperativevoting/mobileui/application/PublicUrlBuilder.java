package com.example.cooperativevoting.mobileui.application;

import com.example.cooperativevoting.shared.config.AppProperties;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PublicUrlBuilder {
  private final String baseUrl;

  public PublicUrlBuilder(AppProperties properties) {
    this.baseUrl = stripTrailingSlash(properties.publicBaseUrl());
  }

  public String path(String path) {
    return baseUrl + (path.startsWith("/") ? path : "/" + path);
  }

  public String path(String template, UUID agendaId) {
    return path(template.replace("{agendaId}", agendaId.toString()));
  }

  private String stripTrailingSlash(String value) {
    return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
  }
}
