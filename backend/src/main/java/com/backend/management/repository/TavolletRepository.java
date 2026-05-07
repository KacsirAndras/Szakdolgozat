package com.backend.management.repository;

import com.backend.management.enums.TavolletStatusz;
import com.backend.management.enums.TavolletTipus;
import com.backend.management.model.Tavollet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface TavolletRepository extends JpaRepository<Tavollet, Long> {
    List<Tavollet> findAllByOrderByKezdetDesc();

    List<Tavollet> findByMunkatarsEmailIgnoreCaseOrderByKezdetDesc(String email);

    List<Tavollet> findByMunkatarsCsapatTulajdonosEmailIgnoreCaseOrderByKezdetDesc(String email);

    List<Tavollet> findByMunkatarsIdAndTipusAndStatuszInAndKezdetBetween(
            Long munkatarsId,
            TavolletTipus tipus,
            Collection<TavolletStatusz> statuszok,
            LocalDate evKezdete,
            LocalDate evVege
    );

    List<Tavollet> findByMunkatarsIdAndStatuszInAndKezdetLessThanEqualAndVegeGreaterThanEqual(
            Long munkatarsId,
            Collection<TavolletStatusz> statuszok,
            LocalDate vege,
            LocalDate kezdet
    );
}
