package com.victorblasco.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegisterResponse(UUID userId, String email, LocalDateTime createdAt) {}
