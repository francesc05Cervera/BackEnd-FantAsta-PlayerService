package com.example.fantasta.player_service.Exceptions;

public class SynchronizationInProgressException extends RuntimeException {
    public SynchronizationInProgressException() { super("A player synchronization is already in progress"); }
}
