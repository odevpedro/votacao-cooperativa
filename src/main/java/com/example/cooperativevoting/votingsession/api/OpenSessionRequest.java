package com.example.cooperativevoting.votingsession.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

public record OpenSessionRequest(@Positive @Max(1440) Long durationMinutes) {}
