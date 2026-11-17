package com.example.fantasta.player_service.Integration;

import java.time.Year;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api-football")
public class ApiFootballProperties {
    private boolean enabled = false;
    private String key;
    private String baseUrl = "https://v3.football.api-sports.io";
    private int leagueId = 135;
    private int season = Year.now().getValue();
    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 10000;
    private long minRequestIntervalMs = 6500;
    private String cron = "0 0 3 * * MON";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public int getLeagueId() { return leagueId; }
    public void setLeagueId(int leagueId) { this.leagueId = leagueId; }
    public int getSeason() { return season; }
    public void setSeason(int season) { this.season = season; }
    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
    public long getMinRequestIntervalMs() { return minRequestIntervalMs; }
    public void setMinRequestIntervalMs(long minRequestIntervalMs) { this.minRequestIntervalMs = minRequestIntervalMs; }
    public String getCron() { return cron; }
    public void setCron(String cron) { this.cron = cron; }
}
