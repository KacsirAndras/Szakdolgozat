package com.backend.management.repository;

import com.backend.management.enums.DayOffStatus;
import com.backend.management.enums.DayOffType;
import com.backend.management.model.DayOff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface DayOffRepository extends JpaRepository<DayOff, Long> {
    List<DayOff> findAllByOrderByStartDateDesc();

    List<DayOff> findByEmployeeEmailIgnoreCaseOrderByStartDateDesc(String email);

    List<DayOff> findByEmployeeTeamOwnerEmailIgnoreCaseOrderByStartDateDesc(String email);

    List<DayOff> findByEmployeeIdAndTypeAndStatusInAndStartDateBetween(
            Long employeeId,
            DayOffType type,
            Collection<DayOffStatus> statuses,
            LocalDate evKezdete,
            LocalDate evVege
    );

    List<DayOff> findByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long employeeId,
            Collection<DayOffStatus> statuses,
            LocalDate endDate,
            LocalDate startDate
    );
}
