package com.example.fantasta.player_service.Exceptions;

public class FootballApiException extends RuntimeException {
    public FootballApiException(String message) { super(message); }
    public FootballApiException(String message, Throwable cause) { super(message, cause); }
}
