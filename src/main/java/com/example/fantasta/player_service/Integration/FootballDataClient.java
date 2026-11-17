package com.example.fantasta.player_service.Integration;

import java.util.List;

public interface FootballDataClient {
    List<ApiFootballTeam> getLeagueTeams(int leagueId, int season);
    List<ExternalFootballPlayer> getTeamSquad(ApiFootballTeam team);
}
