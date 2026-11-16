package com.example.fantasta.player_service.Exceptions;

public class InvalidPlayerRequestException extends RuntimeException {
    public InvalidPlayerRequestException(String message) {
        super(message);
    }
}
