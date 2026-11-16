package com.example.fantasta.player_service.Controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.fantasta.player_service.DTO.ChangeInfoPlayerRequest;
import com.example.fantasta.player_service.DTO.PlayerRequest;
import com.example.fantasta.player_service.DTO.PlayerResponse;
import com.example.fantasta.player_service.Exceptions.DuplicatePlayerException;
import com.example.fantasta.player_service.Exceptions.NotFoundException;
import com.example.fantasta.player_service.Exceptions.TokenException;
import com.example.fantasta.player_service.Service.PlayerService;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    private final PlayerService playerService;
    public PlayerController(PlayerService playerService) { this.playerService = playerService; }
    @PostMapping public ResponseEntity<PlayerResponse> createPlayer(@RequestHeader("Authorization") String header, @Valid @RequestBody PlayerRequest request) throws TokenException, DuplicatePlayerException { return ResponseEntity.ok(playerService.createPlayer(header, request)); }
    @GetMapping public List<PlayerResponse> listPlayers(@RequestHeader("Authorization") String header) throws TokenException { return playerService.listPlayers(header); }
    @GetMapping("/{id}") public PlayerResponse getPlayerById(@RequestHeader("Authorization") String header, @PathVariable Integer id) throws TokenException, NotFoundException { return playerService.getPlayerById(header, id); }
    @GetMapping("/role/{role}") public List<PlayerResponse> listPlayersByRole(@RequestHeader("Authorization") String header, @PathVariable String role) throws TokenException { return playerService.listPlayersByRole(header, role); }
    @GetMapping("/team/{team}") public List<PlayerResponse> listPlayersByTeam(@RequestHeader("Authorization") String header, @PathVariable String team) throws TokenException { return playerService.listPlayersByTeam(header, team); }
    @GetMapping("/role/{role}/team/{team}") public List<PlayerResponse> listPlayersByRoleAndTeam(@RequestHeader("Authorization") String header, @PathVariable String role, @PathVariable String team) throws TokenException { return playerService.listPlayersByRoleAndTeam(header, role, team); }
    @PutMapping("/{id}/role") public PlayerResponse updatePlayerRole(@RequestHeader("Authorization") String header, @PathVariable Integer id, @RequestParam String newRole) throws TokenException, NotFoundException, DuplicatePlayerException { return playerService.updatePlayerRole(header, id, newRole); }
    @PostMapping("/import-csv") public List<PlayerResponse> importPlayersFromCsv(@RequestHeader("Authorization") String header, @RequestParam("file") MultipartFile file) throws TokenException, DuplicatePlayerException { return playerService.importPlayersFromCsv(header, file); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> deletePlayer(@RequestHeader("Authorization") String header, @PathVariable Integer id) throws TokenException, NotFoundException { playerService.removePlayer(header, id); return ResponseEntity.noContent().build(); }
    @DeleteMapping("/all") public ResponseEntity<Void> deleteAllPlayers(@RequestHeader("Authorization") String header) throws TokenException { playerService.removeAll(header); return ResponseEntity.noContent().build(); }
    @PutMapping("/{id}/all-info") public PlayerResponse updatePlayerInfo(@RequestHeader("Authorization") String header, @PathVariable Integer id, @Valid @RequestBody ChangeInfoPlayerRequest request) throws TokenException, NotFoundException, DuplicatePlayerException { return playerService.updatePlayerInfo(header, id, request); }
}
