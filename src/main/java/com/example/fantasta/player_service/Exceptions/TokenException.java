package com.example.fantasta.player_service.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class TokenException extends Exception
{
    public TokenException(String message) {
        super(message);
    }
}
