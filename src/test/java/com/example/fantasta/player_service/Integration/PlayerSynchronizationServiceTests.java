package com.example.fantasta.player_service.Integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.example.fantasta.player_service.DTO.PlayerSyncResponse;
import com.example.fantasta.player_service.Entity.Player;
import com.example.fantasta.player_service.Exceptions.FootballApiException;
import com.example.fantasta.player_service.Repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlayerSynchronizationServiceTests {
    private PlayerRepository repository;
    private FootballDataClient api;
    private ApiFootballProperties properties;
    private PlayerSynchronizationService service;

    @BeforeEach
    void setUp() {
        repository = mock(PlayerRepository.class);
        api = mock(FootballDataClient.class);
        properties = new ApiFootballProperties(); properties.setEnabled(true);
        service = new PlayerSynchronizationService(repository, api, properties);
        when(api.getLeagueTeams(135, properties.getSeason())).thenReturn(List.of(new ApiFootballTeam(1, "Roma")));
    }

    @Test
    void insertsApiPlayersAndMapsCanonicalRole() {
        ExternalFootballPlayer external = new ExternalFootballPlayer(10, "Paulo", "Dybala", "A", "Roma");
        when(api.getTeamSquad(any())).thenReturn(List.of(external));
        when(repository.findByExternalPlayerId(10L)).thenReturn(Optional.empty());
        when(repository.findByNormalizedNameAndSurname("Paulo", "Dybala")).thenReturn(List.of());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        PlayerSyncResponse result = service.synchronize();
        assertEquals(new PlayerSyncResponse(1, 0, 0, 1), result);
        verify(repository).save(argThat(player -> "A".equals(player.getRole()) && Long.valueOf(10).equals(player.getExternalPlayerId())));
    }

    @Test
    void updatesTransferWithoutChangingInternalId() {
        Player existing = player(7, 10L, "Paulo", "Dybala", "A", "Roma");
        ExternalFootballPlayer transfer = new ExternalFootballPlayer(10, "Paulo", "Dybala", "A", "Inter");
        when(api.getTeamSquad(any())).thenReturn(List.of(transfer));
        when(repository.findByExternalPlayerId(10L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        PlayerSyncResponse result = service.synchronize();
        assertEquals(1, result.updated()); assertEquals(7, existing.getId()); assertEquals("Inter", existing.getTeam());
    }

    @Test
    void adoptsOneReliableCsvMatchWithoutCreatingDuplicate() {
        Player legacy = player(4, null, "Paulo", "Dybala", "ATTACCANTE", "Roma");
        ExternalFootballPlayer external = new ExternalFootballPlayer(10, "Paulo", "Dybala", "A", "Roma");
        when(api.getTeamSquad(any())).thenReturn(List.of(external));
        when(repository.findByExternalPlayerId(10L)).thenReturn(Optional.empty());
        when(repository.findByNormalizedNameAndSurname("Paulo", "Dybala")).thenReturn(List.of(legacy));
        when(repository.save(legacy)).thenReturn(legacy);
        PlayerSyncResponse result = service.synchronize();
        assertEquals(1, result.updated()); assertEquals(4, legacy.getId()); assertEquals(Long.valueOf(10), legacy.getExternalPlayerId());
    }

    @Test
    void apiFailureLeavesRepositoryUntouched() {
        when(api.getLeagueTeams(anyInt(), anyInt())).thenThrow(new FootballApiException("timeout"));
        assertThrows(FootballApiException.class, () -> service.synchronize());
        verifyNoInteractions(repository);
    }

    @Test
    void ambiguousLegacyMatchAbortsTheSynchronization() {
        ExternalFootballPlayer external = new ExternalFootballPlayer(10, "Mario", "Rossi", "D", "Roma");
        when(api.getTeamSquad(any())).thenReturn(List.of(external));
        when(repository.findByExternalPlayerId(anyLong())).thenReturn(Optional.empty());
        when(repository.findByNormalizedNameAndSurname("Mario", "Rossi")).thenReturn(List.of(new Player(), new Player()));
        assertThrows(FootballApiException.class, () -> service.synchronize());
        verify(repository, never()).save(any());
    }

    private Player player(int id, Long externalId, String name, String surname, String role, String team) {
        Player player = new Player(); player.setId(id); player.setExternalPlayerId(externalId); player.setName(name);
        player.setSurname(surname); player.setRole(role); player.setTeam(team); return player;
    }
}
