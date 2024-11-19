package com.github.senocak.repository;

import com.github.senocak.model.Team;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, String> {
    @Query(value = "SELECT p FROM Team p WHERE p.id = :idOrSlug or  p.name = :idOrSlug")
    Optional<Team> findByIdOrName(@Param("idOrSlug") String idOrSlug);
}
