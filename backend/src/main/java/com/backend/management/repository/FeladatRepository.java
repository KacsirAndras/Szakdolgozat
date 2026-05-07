package com.backend.management.repository;

import com.backend.management.enums.FeladatStatusz;
import com.backend.management.enums.ProjektStatusz;
import com.backend.management.model.Feladat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeladatRepository extends JpaRepository<Feladat, Long> {
    List<Feladat> findByProjektId(Long projektId);

    List<Feladat> findByProjektTermekTulajdonosEmailIgnoreCaseAndProjektStatusz(String email, ProjektStatusz statusz);

    List<Feladat> findByProjektCsapatIdAndProjektStatusz(Long csapatId, ProjektStatusz statusz);

    List<Feladat> findByMunkatarsIdAndStatuszNot(Long munkatarsId, FeladatStatusz statusz);

    void deleteByProjektId(Long projektId);
}
