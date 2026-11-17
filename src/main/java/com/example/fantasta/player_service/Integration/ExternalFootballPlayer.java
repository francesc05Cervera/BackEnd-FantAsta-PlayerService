package com.example.fantasta.player_service.Integration;

/** Normalized, provider-independent snapshot of a roster entry. */
public record ExternalFootballPlayer(long externalId, String name, String surname, String role, String team) { }
