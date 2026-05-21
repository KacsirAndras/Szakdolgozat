package com.backend.management.repository;

import com.backend.management.enums.ProjectStatus;
import com.backend.management.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByProductOwnerEmailIgnoreCaseAndStatusNot(String email, ProjectStatus status);

    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByCreatorEmailIgnoreCase(String email);

    List<Project> findByProductOwnerEmailIgnoreCase(String email);

    List<Project> findByProductOwnerIdAndStatus(Long productOwnerId, ProjectStatus status);

    List<Project> findByTeamId(Long teamId);
}
