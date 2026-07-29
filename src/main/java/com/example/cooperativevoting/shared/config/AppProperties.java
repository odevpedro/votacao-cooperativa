package com.example.cooperativevoting.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app")
public record AppProperties(String publicBaseUrl) {}
