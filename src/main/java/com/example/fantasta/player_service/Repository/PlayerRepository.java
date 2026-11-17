package com.example.fantasta.player_service.Repository;

import com.example.fantasta.player_service.Entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Integer> {

    List<Player> findByRole(String role);

    List<Player> findByTeam(String team);

    List<Player> findByRoleAndTeam(String role, String team);

    List<Player> findBySurname(String surname);

    @Query("""
            select (count(p) > 0) from Player p
            where lower(p.name) = lower(:name) and lower(p.surname) = lower(:surname)
              and lower(p.role) = lower(:role)
              and ((:team is null and p.team is null) or lower(p.team) = lower(:team))
              and (:excludedId is null or p.id <> :excludedId)
            """)
    boolean existsNormalized(@Param("name") String name, @Param("surname") String surname,
            @Param("role") String role, @Param("team") String team,
            @Param("excludedId") Integer excludedId);

    Optional<Player> findByExternalPlayerId(Long externalPlayerId);

    @Query("select p from Player p where lower(p.name) = lower(:name) and lower(p.surname) = lower(:surname)")
    List<Player> findByNormalizedNameAndSurname(@Param("name") String name, @Param("surname") String surname);
}
