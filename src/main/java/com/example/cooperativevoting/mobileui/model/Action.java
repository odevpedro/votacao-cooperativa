package com.example.cooperativevoting.mobileui.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Action(String texto, String url, Map<String, Object> body) {}
