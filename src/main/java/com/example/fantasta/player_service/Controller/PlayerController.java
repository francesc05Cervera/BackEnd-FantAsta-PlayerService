package com.example.fantasta.player_service.Controller;

import com.example.fantasta.player_service.DTO.PlayerRequest;
import com.example.fantasta.player_service.DTO.PlayerResponse;
import com.example.fantasta.player_service.Service.PlayerService;
import com.example.fantasta.player_service.Exceptions.TokenException;
import com.example.fantasta.player_service.Exceptions.NotFoundException;
import com.example.fantasta.player_service.Exceptions.DuplicatePlayerException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    // 1. POST /api/players
    @PostMapping
    public ResponseEntity<?> createPlayer(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody PlayerRequest request) {

        try {
            PlayerResponse player = playerService.createPlayer(authorizationHeader, request);
            return ResponseEntity.ok(player);
        } catch (TokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 2. GET /api/players
    @GetMapping
    public ResponseEntity<?> listPlayers(
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            List<PlayerResponse> players = playerService.listPlayers(authorizationHeader);
            return ResponseEntity.ok(players);
        } catch (TokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // 3. GET /api/players/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getPlayerById(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Integer id) {

        try {
            PlayerResponse player = playerService.getPlayerById(authorizationHeader, id);
            return ResponseEntity.ok(player);
        } catch (TokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // 4. GET /api/players/role/{role}
    @GetMapping("/role/{role}")
    public ResponseEntity<?> listPlayersByRole(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String role) {

        try {
            List<PlayerResponse> players = playerService.listPlayersByRole(authorizationHeader, role);
            return ResponseEntity.ok(players);
        } catch (TokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // 5. GET /api/players/team/{team}
    @GetMapping("/team/{team}")
    public ResponseEntity<?> listPlayersByTeam(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String team) {

        try {
            List<PlayerResponse> players = playerService.listPlayersByTeam(authorizationHeader, team);
            return ResponseEntity.ok(players);
        } catch (TokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // 6. GET /api/players/role/{role}/team/{team}
    @GetMapping("/role/{role}/team/{team}")
    public ResponseEntity<?> listPlayersByRoleAndTeam(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String role,
            @PathVariable String team) {

        try {
            List<PlayerResponse> players = playerService.listPlayersByRoleAndTeam(authorizationHeader, role, team);
            return ResponseEntity.ok(players);
        } catch (TokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // 7. PUT /api/players/{id}/role
    @PutMapping("/{id}/role")
    public ResponseEntity<?> updatePlayerRole(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Integer id,
            @RequestParam String newRole) {

        try {
            PlayerResponse player = playerService.updatePlayerRole(authorizationHeader, id, newRole);
            return ResponseEntity.ok(player);
        } catch (TokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (DuplicatePlayerException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 8. POST /api/players/import-csv
    @PostMapping("/import-csv")
    public ResponseEntity<?> importPlayersFromCsv(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam("file") MultipartFile file) {

        try {
            List<PlayerResponse> players = playerService.importPlayersFromCsv(authorizationHeader, file);
            return ResponseEntity.ok(players);
        } catch (TokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (DuplicatePlayerException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Errore nell'import: " + e.getMessage());
        }
    }
}