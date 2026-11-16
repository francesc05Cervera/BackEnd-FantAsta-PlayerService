package com.example.fantasta.player_service.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.fantasta.player_service.Client.AuthServiceClient;
import com.example.fantasta.player_service.DTO.AuthUserResponse;
import com.example.fantasta.player_service.DTO.ChangeInfoPlayerRequest;
import com.example.fantasta.player_service.DTO.PlayerRequest;
import com.example.fantasta.player_service.DTO.PlayerResponse;
import com.example.fantasta.player_service.Entity.Player;
import com.example.fantasta.player_service.Exceptions.DuplicatePlayerException;
import com.example.fantasta.player_service.Exceptions.ForbiddenException;
import com.example.fantasta.player_service.Exceptions.NotFoundException;
import com.example.fantasta.player_service.Exceptions.TokenException;
import com.example.fantasta.player_service.Repository.PlayerRepository;

@Service
public class PlayerService {
    private static final List<String> ROLES = List.of("PORTIERE", "DIFENSORE", "CENTROCAMPISTA", "ATTACCANTE");
    private final PlayerRepository playerRepository;
    private final AuthServiceClient authServiceClient;

    public PlayerService(PlayerRepository playerRepository, AuthServiceClient authServiceClient) {
        this.playerRepository = playerRepository;
        this.authServiceClient = authServiceClient;
    }

    private AuthUserResponse authenticate(String authorizationHeader) throws TokenException {
        return authServiceClient.getAuthenticatedUser(authorizationHeader);
    }

    private void requireAdmin(String authorizationHeader) throws TokenException {
        if (!"ADMIN".equals(authenticate(authorizationHeader).getRole())) {
            throw new ForbiddenException("This operation requires an administrator role.");
        }
    }

    private String normalizeRole(String role) throws DuplicatePlayerException {
        String normalized = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
        if (!ROLES.contains(normalized)) throw new DuplicatePlayerException("Role must be one of " + ROLES);
        return normalized;
    }

    private String normalizeOptional(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private PlayerResponse toResponse(Player player) {
        PlayerResponse response = new PlayerResponse();
        response.setId(player.getId()); response.setName(player.getName()); response.setSurname(player.getSurname());
        response.setRole(player.getRole()); response.setTeam(player.getTeam());
        return response;
    }

    private Player saveNew(PlayerRequest request) throws DuplicatePlayerException {
        String name = request.getName().trim();
        String surname = request.getSurname().trim();
        String role = normalizeRole(request.getRole());
        String team = normalizeOptional(request.getTeam());
        if (playerRepository.existsByNameIgnoreCaseAndSurnameIgnoreCaseAndRoleIgnoreCaseAndTeamIgnoreCase(name, surname, role, team)) {
            throw new DuplicatePlayerException("Player already exists");
        }
        Player player = new Player();
        player.setName(name); player.setSurname(surname); player.setRole(role); player.setTeam(team);
        return playerRepository.save(player);
    }

    @Transactional(rollbackFor = Exception.class)
    public PlayerResponse createPlayer(String authorizationHeader, PlayerRequest request) throws TokenException, DuplicatePlayerException {
        requireAdmin(authorizationHeader);
        return toResponse(saveNew(request));
    }

    public List<PlayerResponse> listPlayers(String authorizationHeader) throws TokenException {
        authenticate(authorizationHeader);
        return playerRepository.findAll().stream().map(this::toResponse).toList();
    }

    public PlayerResponse getPlayerById(String authorizationHeader, Integer playerId) throws TokenException, NotFoundException {
        authenticate(authorizationHeader); return toResponse(findPlayer(playerId));
    }

    public List<PlayerResponse> listPlayersByRole(String authorizationHeader, String role) throws TokenException {
        authenticate(authorizationHeader);
        return playerRepository.findByRole(role.trim().toUpperCase(Locale.ROOT)).stream().map(this::toResponse).toList();
    }

    public List<PlayerResponse> listPlayersByTeam(String authorizationHeader, String team) throws TokenException {
        authenticate(authorizationHeader); return playerRepository.findByTeam(team.trim()).stream().map(this::toResponse).toList();
    }

    public List<PlayerResponse> listPlayersByRoleAndTeam(String authorizationHeader, String role, String team) throws TokenException {
        authenticate(authorizationHeader);
        return playerRepository.findByRoleAndTeam(role.trim().toUpperCase(Locale.ROOT), team.trim()).stream().map(this::toResponse).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public PlayerResponse updatePlayerRole(String authorizationHeader, Integer playerId, String newRole) throws TokenException, NotFoundException, DuplicatePlayerException {
        requireAdmin(authorizationHeader); Player player = findPlayer(playerId); player.setRole(normalizeRole(newRole));
        return toResponse(playerRepository.save(player));
    }

    @Transactional(rollbackFor = Exception.class)
    public List<PlayerResponse> importPlayersFromCsv(String authorizationHeader, MultipartFile file) throws TokenException, DuplicatePlayerException {
        requireAdmin(authorizationHeader);
        if (file == null || file.isEmpty()) throw new DuplicatePlayerException("CSV file is empty");
        List<PlayerResponse> players = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line; int row = 0;
            while ((line = reader.readLine()) != null) {
                row++; if (row == 1 && line.toLowerCase(Locale.ROOT).contains("name")) continue; if (line.isBlank()) continue;
                List<String> columns = parseCsv(line, row);
                if (columns.size() < 3 || columns.size() > 4) throw new DuplicatePlayerException("Invalid CSV row " + row + ": expected 3 or 4 columns");
                PlayerRequest request = new PlayerRequest();
                request.setName(columns.get(0)); request.setSurname(columns.get(1)); request.setRole(columns.get(2));
                request.setTeam(columns.size() == 4 ? columns.get(3) : null);
                players.add(toResponse(saveNew(request)));
            }
        } catch (IOException exception) { throw new DuplicatePlayerException("Unable to read CSV file"); }
        return players;
    }

    private List<String> parseCsv(String line, int row) throws DuplicatePlayerException {
        List<String> result = new ArrayList<>(); StringBuilder value = new StringBuilder(); boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char current = line.charAt(index);
            if (current == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') { value.append('"'); index++; }
                else quoted = !quoted;
            } else if (current == ',' && !quoted) { result.add(value.toString().trim()); value.setLength(0); }
            else value.append(current);
        }
        if (quoted) throw new DuplicatePlayerException("Invalid CSV row " + row + ": unclosed quote");
        result.add(value.toString().trim()); return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void removePlayer(String authorizationHeader, Integer playerId) throws TokenException, NotFoundException { requireAdmin(authorizationHeader); playerRepository.delete(findPlayer(playerId)); }

    @Transactional(rollbackFor = Exception.class)
    public void removeAll(String authorizationHeader) throws TokenException { requireAdmin(authorizationHeader); playerRepository.deleteAll(); }

    @Transactional(rollbackFor = Exception.class)
    public PlayerResponse updatePlayerInfo(String authorizationHeader, Integer playerId, ChangeInfoPlayerRequest request) throws TokenException, NotFoundException, DuplicatePlayerException {
        requireAdmin(authorizationHeader); Player player = findPlayer(playerId);
        if (request.getName() != null && !request.getName().isBlank()) player.setName(request.getName().trim());
        if (request.getSurname() != null && !request.getSurname().isBlank()) player.setSurname(request.getSurname().trim());
        if (request.getRole() != null && !request.getRole().isBlank()) player.setRole(normalizeRole(request.getRole()));
        if (request.getTeam() != null) player.setTeam(normalizeOptional(request.getTeam()));
        return toResponse(playerRepository.save(player));
    }

    private Player findPlayer(Integer playerId) throws NotFoundException {
        return playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
    }
}
