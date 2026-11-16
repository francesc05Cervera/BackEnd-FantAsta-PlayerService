package com.example.fantasta.player_service.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.fantasta.player_service.Client.AuthServiceClient;
import com.example.fantasta.player_service.DTO.AuthUserResponse;
import com.example.fantasta.player_service.DTO.PlayerRequest;
import com.example.fantasta.player_service.Entity.Player;
import com.example.fantasta.player_service.Exceptions.ForbiddenException;
import com.example.fantasta.player_service.Repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class PlayerServiceAuthorizationTests {
    @Test
    void regularUserCannotCreatePlayers() throws Exception {
        PlayerRepository repository = mock(PlayerRepository.class);
        AuthServiceClient auth = mock(AuthServiceClient.class);
        when(auth.getAuthenticatedUser("Bearer user")).thenReturn(user("USER"));
        PlayerService service = new PlayerService(repository, auth);
        PlayerRequest request = request("Mario", "Rossi", "ATTACCANTE", "Roma");
        assertThrows(ForbiddenException.class, () -> service.createPlayer("Bearer user", request));
    }

    @Test
    void adminImportsQuotedCsvFields() throws Exception {
        PlayerRepository repository = mock(PlayerRepository.class);
        AuthServiceClient auth = mock(AuthServiceClient.class);
        when(auth.getAuthenticatedUser("Bearer admin")).thenReturn(user("ADMIN"));
        when(repository.existsByNameIgnoreCaseAndSurnameIgnoreCaseAndRoleIgnoreCaseAndTeamIgnoreCase(any(), any(), any(), any())).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> { Player player = invocation.getArgument(0); player.setId(1); return player; });
        PlayerService service = new PlayerService(repository, auth);
        MockMultipartFile csv = new MockMultipartFile("file", "players.csv", "text/csv",
                "name,surname,role,team\n\"Mario, Jr\",Rossi,ATTACCANTE,\"A.S. Roma\"".getBytes());
        assertEquals(1, service.importPlayersFromCsv("Bearer admin", csv).size());
    }

    private AuthUserResponse user(String role) { AuthUserResponse user = new AuthUserResponse(1L, "a@b.test", "A", "B"); user.setRole(role); return user; }
    private PlayerRequest request(String name, String surname, String role, String team) { PlayerRequest request = new PlayerRequest(); request.setName(name); request.setSurname(surname); request.setRole(role); request.setTeam(team); return request; }
}
