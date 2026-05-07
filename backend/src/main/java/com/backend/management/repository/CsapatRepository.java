package com.backend.management.repository;

import com.backend.management.model.Csapat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CsapatRepository extends JpaRepository<Csapat, Long> {
    Optional<Csapat> findByTulajdonosEmailIgnoreCase(String email);

    Optional<Csapat> findByTulajdonosId(Long ownerId);
}
