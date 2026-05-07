package com.backend.management.repository;

import com.backend.management.model.Felhasznalo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FelhasznaloRepository extends JpaRepository<Felhasznalo, Long> {
    Optional<Felhasznalo> findByEmailIgnoreCase(String email);

    Optional<Felhasznalo> findByVisszaallitoKod(String visszaallitoKod);

    Optional<Felhasznalo> findByAktivaloKod(String aktivaloKod);
}
