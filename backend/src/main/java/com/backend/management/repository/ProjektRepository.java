package com.backend.management.repository;

import com.backend.management.enums.ProjektStatusz;
import com.backend.management.model.Projekt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjektRepository extends JpaRepository<Projekt, Long> {
    boolean existsByTermekTulajdonosEmailIgnoreCaseAndStatuszNot(String email, ProjektStatusz statusz);

    List<Projekt> findByStatusz(ProjektStatusz statusz);

    List<Projekt> findByLetrehozoEmailIgnoreCase(String email);

    List<Projekt> findByTermekTulajdonosEmailIgnoreCase(String email);

    List<Projekt> findByTermekTulajdonosIdAndStatusz(Long termekTulajdonosId, ProjektStatusz statusz);
}
