package com.backend.management.repository;

import com.backend.management.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByOwnerEmailIgnoreCase(String email);

    Optional<Team> findByOwnerId(Long ownerId);
}
