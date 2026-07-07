package com.example.fantasta.player_service.Repository;

import com.example.fantasta.player_service.Entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Integer> {

    List<Player> findByRole(String role);

    List<Player> findBySurname(String surname);

    List<Player> findByRoleAndSurname(String role, String surname);

    List<Player> findByTeam(String team);

    List<Player> findByRoleAndTeam(String role, String team);

    List<Player> findBySurnameAndTeam(String surname, String team);

}