package com.example.fantasta.player_service.Integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import com.example.fantasta.player_service.Exceptions.FootballApiException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class ApiFootballClient implements FootballDataClient {
    private final RestClient restClient;
    private final ApiFootballProperties properties;
    private final AtomicLong lastRequestAt = new AtomicLong(0);

    public ApiFootballClient(RestClient apiFootballRestClient, ApiFootballProperties properties) {
        this.restClient = apiFootballRestClient;
        this.properties = properties;
    }

    @Override
    public List<ApiFootballTeam> getLeagueTeams(int leagueId, int season) {
        TeamsResponse response = get("/teams?league={league}&season={season}", TeamsResponse.class, leagueId, season);
        if (response == null || response.response() == null) throw new FootballApiException("API-Football returned an invalid teams response");
        return response.response().stream().filter(item -> item.team() != null && item.team().id() != null && item.team().name() != null)
                .map(item -> new ApiFootballTeam(item.team().id(), item.team().name().trim())).toList();
    }

    @Override
    public List<ExternalFootballPlayer> getTeamSquad(ApiFootballTeam team) {
        SquadsResponse response = get("/players/squads?team={team}", SquadsResponse.class, team.id());
        if (response == null || response.response() == null) throw new FootballApiException("API-Football returned an invalid squad response");
        List<ExternalFootballPlayer> players = new ArrayList<>();
        for (SquadEnvelope squad : response.response()) {
            if (squad.players() == null) continue;
            for (ApiPlayer player : squad.players()) players.add(toExternalPlayer(player, team.name()));
        }
        return players;
    }

    private ExternalFootballPlayer toExternalPlayer(ApiPlayer player, String teamName) {
        if (player.id() == null || player.id() <= 0 || player.name() == null || player.name().isBlank()) {
            throw new FootballApiException("API-Football returned a player without a valid identifier or name");
        }
        String role = switch (player.position() == null ? "" : player.position().trim().toUpperCase(Locale.ROOT)) {
            case "GOALKEEPER" -> "P";
            case "DEFENDER" -> "D";
            case "MIDFIELDER" -> "C";
            case "ATTACKER" -> "A";
            default -> throw new FootballApiException("Unsupported API-Football player position: " + player.position());
        };
        String firstName = nonBlank(player.firstname());
        String lastName = nonBlank(player.lastname());
        if (lastName == null) {
            String[] parts = player.name().trim().split("\\s+", 2);
            firstName = parts[0];
            lastName = parts.length == 2 ? parts[1] : parts[0];
        }
        if (firstName == null) firstName = lastName;
        return new ExternalFootballPlayer(player.id(), firstName, lastName, role, teamName);
    }

    private String nonBlank(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private <T> T get(String uri, Class<T> responseType, Object... uriVariables) {
        if (!properties.isEnabled()) throw new FootballApiException("API-Football synchronization is disabled");
        if (properties.getKey() == null || properties.getKey().isBlank()) throw new FootballApiException("API_FOOTBALL_KEY is not configured");
        waitForRateLimit();
        try {
            return restClient.get().uri(uri, uriVariables).header("x-apisports-key", properties.getKey()).retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new FootballApiException(response.getStatusCode().value() == 429
                                ? "API-Football rate limit exceeded" : "API-Football returned HTTP " + response.getStatusCode().value());
                    }).body(responseType);
        } catch (FootballApiException exception) { throw exception;
        } catch (RestClientResponseException exception) {
            throw new FootballApiException(exception.getStatusCode().value() == 429 ? "API-Football rate limit exceeded" : "API-Football returned an error", exception);
        } catch (ResourceAccessException exception) { throw new FootballApiException("API-Football is unavailable or timed out", exception);
        } catch (RestClientException exception) { throw new FootballApiException("API-Football returned an invalid response", exception); }
    }

    private void waitForRateLimit() {
        long interval = Math.max(0, properties.getMinRequestIntervalMs());
        synchronized (lastRequestAt) {
            long wait = interval - (System.currentTimeMillis() - lastRequestAt.get());
            if (wait > 0) try { Thread.sleep(wait); } catch (InterruptedException exception) {
                Thread.currentThread().interrupt(); throw new FootballApiException("API-Football request interrupted", exception);
            }
            lastRequestAt.set(System.currentTimeMillis());
        }
    }

    private record TeamsResponse(List<TeamEnvelope> response) { }
    private record TeamEnvelope(Team team) { }
    private record Team(Long id, String name) { }
    private record SquadsResponse(List<SquadEnvelope> response) { }
    private record SquadEnvelope(List<ApiPlayer> players) { }
    private record ApiPlayer(Long id, String name, String firstname, String lastname, String position) { }
}
