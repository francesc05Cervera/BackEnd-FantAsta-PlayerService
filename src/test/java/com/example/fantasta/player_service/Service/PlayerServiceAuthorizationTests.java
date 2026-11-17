package com.example.fantasta.player_service.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.fantasta.player_service.Client.AuthServiceClient;
import com.example.fantasta.player_service.DTO.AuthUserResponse;
import com.example.fantasta.player_service.DTO.PlayerRequest;
import com.example.fantasta.player_service.Entity.Player;
import com.example.fantasta.player_service.Exceptions.ForbiddenException;
import com.example.fantasta.player_service.Exceptions.DuplicatePlayerException;
import com.example.fantasta.player_service.Repository.PlayerRepository;
import com.example.fantasta.player_service.Integration.PlayerSynchronizationService;
import com.example.fantasta.player_service.DTO.PlayerSyncResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class PlayerServiceAuthorizationTests {
    @Test
    void regularUserCannotCreatePlayers() throws Exception {
        PlayerRepository repository = mock(PlayerRepository.class);
        AuthServiceClient auth = mock(AuthServiceClient.class);
        when(auth.getAuthenticatedUser("Bearer user")).thenReturn(user("USER"));
        PlayerService service = new PlayerService(repository, auth, mock(PlayerSynchronizationService.class));
        PlayerRequest request = request("Mario", "Rossi", "ATTACCANTE", "Roma");
        assertThrows(ForbiddenException.class, () -> service.createPlayer("Bearer user", request));
    }

    @Test
    void regularUserCannotStartApiFootballSynchronization() throws Exception {
        PlayerRepository repository = mock(PlayerRepository.class);
        AuthServiceClient auth = mock(AuthServiceClient.class);
        PlayerSynchronizationService synchronization = mock(PlayerSynchronizationService.class);
        when(auth.getAuthenticatedUser("Bearer user")).thenReturn(user("USER"));
        PlayerService service = new PlayerService(repository, auth, synchronization);
        assertThrows(ForbiddenException.class, () -> service.synchronizeFromApiFootball("Bearer user"));
        verify(synchronization, never()).synchronize();
    }

    @Test
    void adminCanStartApiFootballSynchronization() throws Exception {
        PlayerRepository repository = mock(PlayerRepository.class);
        AuthServiceClient auth = mock(AuthServiceClient.class);
        PlayerSynchronizationService synchronization = mock(PlayerSynchronizationService.class);
        when(auth.getAuthenticatedUser("Bearer admin")).thenReturn(user("ADMIN"));
        when(synchronization.synchronize()).thenReturn(new PlayerSyncResponse(1, 2, 3, 6));
        PlayerService service = new PlayerService(repository, auth, synchronization);
        assertEquals(6, service.synchronizeFromApiFootball("Bearer admin").total());
        verify(synchronization).synchronize();
    }

    @Test
    void adminImportsQuotedCsvFields() throws Exception {
        PlayerRepository repository = mock(PlayerRepository.class);
        AuthServiceClient auth = mock(AuthServiceClient.class);
        when(auth.getAuthenticatedUser("Bearer admin")).thenReturn(user("ADMIN"));
        when(repository.existsNormalized(any(), any(), any(), any(), any())).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> { Player player = invocation.getArgument(0); player.setId(1); return player; });
        PlayerService service = new PlayerService(repository, auth, mock(PlayerSynchronizationService.class));
        MockMultipartFile csv = new MockMultipartFile("file", "players.csv", "text/csv",
                "name,surname,role,team\n\"Mario, Jr\",Rossi,ATTACCANTE,\"A.S. Roma\"".getBytes());
        assertEquals(1, service.importPlayersFromCsv("Bearer admin", csv).size());
    }

    @Test
    void importSupportsMultilineAndEscapedQuoteFields() throws Exception {
        PlayerRepository repository = mock(PlayerRepository.class);
        AuthServiceClient auth = mock(AuthServiceClient.class);
        when(auth.getAuthenticatedUser("Bearer admin")).thenReturn(user("ADMIN"));
        when(repository.existsNormalized(any(), any(), any(), any(), any())).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> { Player player = invocation.getArgument(0); player.setId(2); return player; });
        PlayerService service = new PlayerService(repository, auth, mock(PlayerSynchronizationService.class));
        MockMultipartFile csv = new MockMultipartFile("file", "players.csv", "text/csv",
                "name,surname,role,team\r\n\"Mario\n\"\"Super\"\"\",Rossi,ATTACCANTE,Roma\r\n".getBytes());
        assertEquals(1, service.importPlayersFromCsv("Bearer admin", csv).size());
    }

    @Test
    void duplicateWithoutTeamIsRejected() throws Exception {
        PlayerRepository repository = mock(PlayerRepository.class);
        AuthServiceClient auth = mock(AuthServiceClient.class);
        when(auth.getAuthenticatedUser("Bearer admin")).thenReturn(user("ADMIN"));
        when(repository.existsNormalized(any(), any(), any(), isNull(), isNull())).thenReturn(true);
        PlayerService service = new PlayerService(repository, auth, mock(PlayerSynchronizationService.class));
        assertThrows(DuplicatePlayerException.class, () -> service.createPlayer("Bearer admin", request("Mario", "Rossi", "ATTACCANTE", null)));
    }

    private AuthUserResponse user(String role) { AuthUserResponse user = new AuthUserResponse(1L, "a@b.test", "A", "B"); user.setRole(role); return user; }
    private PlayerRequest request(String name, String surname, String role, String team) { PlayerRequest request = new PlayerRequest(); request.setName(name); request.setSurname(surname); request.setRole(role); request.setTeam(team); return request; }
}
