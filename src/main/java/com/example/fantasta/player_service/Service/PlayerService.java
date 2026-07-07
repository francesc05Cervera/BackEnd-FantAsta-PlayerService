package com.example.fantasta.player_service.Service;

import com.example.fantasta.player_service.DTO.AuthUserResponse;
import com.example.fantasta.player_service.DTO.PlayerRequest;
import com.example.fantasta.player_service.DTO.PlayerResponse;
import com.example.fantasta.player_service.Entity.Player;
import com.example.fantasta.player_service.Repository.PlayerRepository;
import com.example.fantasta.player_service.Client.AuthServiceClient;
import com.example.fantasta.player_service.Exceptions.TokenException;
import com.example.fantasta.player_service.Exceptions.NotFoundException;
import com.example.fantasta.player_service.Exceptions.DuplicatePlayerException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final AuthServiceClient authServiceClient;

    public PlayerService(PlayerRepository playerRepository,
                         AuthServiceClient authServiceClient) {
        this.playerRepository = playerRepository;
        this.authServiceClient = authServiceClient;
    }

    private AuthUserResponse validateToken(String authorizationHeader) throws TokenException {
        return authServiceClient.getAuthenticatedUser(authorizationHeader);
    }

    private PlayerResponse toResponse(Player player) {
        PlayerResponse response = new PlayerResponse();
        response.setId(player.getId());
        response.setName(player.getName());
        response.setSurname(player.getSurname());
        response.setRole(player.getRole());
        response.setTeam(player.getTeam());
        return response;
    }

    // 1. Creazione giocatore (globale) - senza controllo duplicazione
    public PlayerResponse createPlayer(String authorizationHeader, PlayerRequest request)
            throws TokenException {

        validateToken(authorizationHeader);

        Player player = new Player();
        player.setName(request.getName());
        player.setSurname(request.getSurname());
        player.setRole(request.getRole());
        player.setTeam(request.getTeam());

        player = playerRepository.save(player);
        return toResponse(player);
    }

    // 2. Lista tutti i giocatori
    public List<PlayerResponse> listPlayers(String authorizationHeader) throws TokenException {
        validateToken(authorizationHeader);

        List<Player> players = playerRepository.findAll();
        return players.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 3. Dettaglio giocatore
    public PlayerResponse getPlayerById(String authorizationHeader, Integer playerId)
            throws TokenException, NotFoundException {

        validateToken(authorizationHeader);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Giocatore con ID " + playerId + " non trovato"));

        return toResponse(player);
    }

    // 4. Filtra per ruolo
    public List<PlayerResponse> listPlayersByRole(String authorizationHeader, String role)
            throws TokenException {

        validateToken(authorizationHeader);

        List<Player> players = playerRepository.findByRole(role);
        return players.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 5. Filtra per team
    public List<PlayerResponse> listPlayersByTeam(String authorizationHeader, String team)
            throws TokenException {

        validateToken(authorizationHeader);

        List<Player> players = playerRepository.findByTeam(team);
        return players.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 6. Filtra per ruolo + team
    public List<PlayerResponse> listPlayersByRoleAndTeam(String authorizationHeader, String role, String team)
            throws TokenException {

        validateToken(authorizationHeader);

        List<Player> players = playerRepository.findByRoleAndTeam(role, team);
        return players.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 7. Cambio ruolo giocatore (globale)
    public PlayerResponse updatePlayerRole(String authorizationHeader, Integer playerId, String newRole)
            throws TokenException, NotFoundException, DuplicatePlayerException {

        validateToken(authorizationHeader);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Giocatore con ID " + playerId + " non trovato"));

        if (newRole == null || newRole.isBlank()) {
            throw new DuplicatePlayerException("Ruolo non valido");
        }

        player.setRole(newRole);
        playerRepository.save(player);

        return toResponse(player);
    }

    // 8. Import da CSV
    public List<PlayerResponse> importPlayersFromCsv(String authorizationHeader, MultipartFile file)
            throws TokenException, DuplicatePlayerException {

        validateToken(authorizationHeader);

        if (file.isEmpty()) {
            throw new DuplicatePlayerException("File CSV vuoto");
        }

        List<PlayerResponse> result = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 3) {
                    continue;
                }

                String name = parts[0].trim();
                String surname = parts[1].trim();
                String role = parts[2].trim();
                String team = parts.length >= 4 ? parts[3].trim() : null;

                PlayerRequest request = new PlayerRequest();
                request.setName(name);
                request.setSurname(surname);
                request.setRole(role);
                request.setTeam(team);

                PlayerResponse player = createPlayer(authorizationHeader, request);
                result.add(player);
            }
        } catch (IOException e) {
            throw new DuplicatePlayerException("Errore lettura file CSV: " + e.getMessage());
        }

        return result;
    }

    public void validateTokenPublic(String authorizationHeader) throws TokenException {
        validateToken(authorizationHeader);
    }
}