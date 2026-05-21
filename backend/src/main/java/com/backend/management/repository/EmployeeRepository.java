package com.backend.management.repository;

import com.backend.management.enums.Role;
import com.backend.management.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmailIgnoreCase(String email);

    List<Employee> findByRoleAndActiveTrueAndTeamIsNull(Role role);
}
