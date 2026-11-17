package com.example.fantasta.player_service.Integration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.example.fantasta.player_service.DTO.PlayerSyncResponse;
import com.example.fantasta.player_service.Entity.Player;
import com.example.fantasta.player_service.Exceptions.FootballApiException;
import com.example.fantasta.player_service.Exceptions.SynchronizationInProgressException;
import com.example.fantasta.player_service.Repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerSynchronizationService {
    private static final Logger log = LoggerFactory.getLogger(PlayerSynchronizationService.class);
    private final PlayerRepository playerRepository;
    private final FootballDataClient footballDataClient;
    private final ApiFootballProperties properties;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public PlayerSynchronizationService(PlayerRepository playerRepository, FootballDataClient footballDataClient,
            ApiFootballProperties properties) {
        this.playerRepository = playerRepository;
        this.footballDataClient = footballDataClient;
        this.properties = properties;
    }

    /** Fetches a complete provider snapshot before changing the database. */
    @Transactional(rollbackFor = Exception.class)
    public PlayerSyncResponse synchronize() {
        if (!properties.isEnabled()) throw new FootballApiException("API-Football synchronization is disabled");
        if (!running.compareAndSet(false, true)) throw new SynchronizationInProgressException();
        try {
            List<ExternalFootballPlayer> snapshot = loadCompleteSnapshot();
            PlayerSyncResponse result = applySnapshot(snapshot);
            log.info("API-Football synchronization completed: inserted={}, updated={}, unchanged={}, total={}",
                    result.inserted(), result.updated(), result.unchanged(), result.total());
            return result;
        } catch (RuntimeException exception) {
            log.warn("API-Football synchronization failed; existing player data was kept", exception);
            throw exception;
        } finally { running.set(false); }
    }

    @Scheduled(cron = "${api-football.cron:0 0 3 * * MON}")
    public void scheduledSynchronization() {
        if (!properties.isEnabled()) return;
        try { synchronize();
        } catch (SynchronizationInProgressException ignored) { log.info("Skipping scheduled API-Football synchronization: one is already running");
        } catch (RuntimeException exception) { log.warn("Scheduled API-Football synchronization failed", exception); }
    }

    private List<ExternalFootballPlayer> loadCompleteSnapshot() {
        List<ApiFootballTeam> teams = footballDataClient.getLeagueTeams(properties.getLeagueId(), properties.getSeason());
        if (teams.isEmpty()) throw new FootballApiException("API-Football returned no teams for the configured league and season");
        List<ExternalFootballPlayer> players = new ArrayList<>();
        Set<Long> ids = new HashSet<>();
        for (ApiFootballTeam team : teams) for (ExternalFootballPlayer player : footballDataClient.getTeamSquad(team)) {
            if (!ids.add(player.externalId())) throw new FootballApiException("API-Football snapshot contains duplicate player id " + player.externalId());
            players.add(player);
        }
        return players;
    }

    private PlayerSyncResponse applySnapshot(List<ExternalFootballPlayer> snapshot) {
        int inserted = 0, updated = 0, unchanged = 0;
        for (ExternalFootballPlayer external : snapshot) {
            Optional<Player> byExternalId = playerRepository.findByExternalPlayerId(external.externalId());
            Player player;
            if (byExternalId.isPresent()) {
                player = byExternalId.get();
                if (matches(player, external)) { unchanged++; continue; }
                copy(external, player); playerRepository.save(player); updated++; continue;
            }
            List<Player> legacyCandidates = playerRepository.findByNormalizedNameAndSurname(external.name(), external.surname());
            if (legacyCandidates.size() > 1) {
                throw new FootballApiException("Ambiguous legacy player match for " + external.name() + " " + external.surname());
            }
            if (legacyCandidates.size() == 1) {
                player = legacyCandidates.get(0);
                if (player.getExternalPlayerId() != null && !player.getExternalPlayerId().equals(external.externalId())) {
                    throw new FootballApiException("Conflicting API-Football identifier for legacy player " + player.getId());
                }
                boolean changed = !matches(player, external);
                copy(external, player); playerRepository.save(player);
                if (changed) updated++; else unchanged++;
                continue;
            }
            player = new Player(); copy(external, player); playerRepository.save(player); inserted++;
        }
        return new PlayerSyncResponse(inserted, updated, unchanged, snapshot.size());
    }

    private boolean matches(Player player, ExternalFootballPlayer external) {
        return external.externalId() == (player.getExternalPlayerId() == null ? -1 : player.getExternalPlayerId())
                && equalsValue(player.getName(), external.name()) && equalsValue(player.getSurname(), external.surname())
                && equalsValue(player.getRole(), external.role()) && equalsValue(player.getTeam(), external.team());
    }
    private boolean equalsValue(String first, String second) { return first == null ? second == null : first.equals(second); }
    private void copy(ExternalFootballPlayer source, Player target) {
        target.setExternalPlayerId(source.externalId()); target.setName(source.name()); target.setSurname(source.surname());
        target.setRole(source.role()); target.setTeam(source.team());
    }
}
