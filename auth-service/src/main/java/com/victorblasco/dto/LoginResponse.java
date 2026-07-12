package com.victorblasco.dto;

public record LoginResponse(String accessToken, String tokenType, int expiresIn) {
    public LoginResponse(String accessToken) {
        this(accessToken, "Bearer", 3600);
    }
}
