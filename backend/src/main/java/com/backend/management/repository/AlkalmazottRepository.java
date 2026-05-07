package com.backend.management.repository;

import com.backend.management.enums.Szerepkor;
import com.backend.management.model.Alkalmazott;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlkalmazottRepository extends JpaRepository<Alkalmazott, Long> {
    Optional<Alkalmazott> findByEmailIgnoreCase(String email);

    List<Alkalmazott> findBySzerepkorAndActiveTrueAndCsapatIsNull(Szerepkor szerepkor);
}
