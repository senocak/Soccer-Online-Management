package com.github.senocak.repository;

import java.util.Optional;
import com.github.senocak.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.PagingAndSortingRepository;

@Repository
public interface PlayerRepository extends PagingAndSortingRepository<Player, String>, JpaSpecificationExecutor<Player>, JpaRepository<Player, String> {
    @Query(value = "SELECT p FROM Player p WHERE p.id = :id")
    Optional<Player> findPlayerById(@Param("id") String id);
    Optional<Player> findPlayerByFirstNameAndLastName(String firstName, String lastName);
}
