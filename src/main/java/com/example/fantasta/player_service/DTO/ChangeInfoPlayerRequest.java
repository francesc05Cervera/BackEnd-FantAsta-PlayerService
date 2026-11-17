package com.example.fantasta.player_service.DTO;

import jakarta.validation.constraints.Size;

public class ChangeInfoPlayerRequest {
    @Size(max = 100) private String name;
    @Size(max = 100) private String surname;
    @Size(max = 50) private String role;
    @Size(max = 100) private String team;

    public ChangeInfoPlayerRequest() {
    }

    public ChangeInfoPlayerRequest(String name, String surname, String role, String team) {
        this.name = name;
        this.surname = surname;
        this.role = role;
        this.team = team;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }
}
