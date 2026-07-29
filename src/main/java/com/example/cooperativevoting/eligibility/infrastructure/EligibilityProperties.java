package com.example.cooperativevoting.eligibility.infrastructure;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.voter-eligibility")
public record EligibilityProperties(
    boolean enabled, String baseUrl, Duration connectTimeout, Duration readTimeout) {}
