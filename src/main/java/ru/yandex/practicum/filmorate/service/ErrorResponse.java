package ru.yandex.practicum.filmorate.service;

import lombok.Getter;

@Getter
public class ErrorResponse {
    private final String error;
    private final String description;

    public ErrorResponse(String error, String description) {
        this.error = "\"error\": \"" + error + "\"";
        this.description = "\"description\": \"" + description + "\"";
    }
}
