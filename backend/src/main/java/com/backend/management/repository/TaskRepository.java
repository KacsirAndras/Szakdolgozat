package com.backend.management.repository;

import com.backend.management.enums.TaskStatus;
import com.backend.management.enums.ProjectStatus;
import com.backend.management.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);

    List<Task> findByProjectProductOwnerEmailIgnoreCaseAndProjectStatus(String email, ProjectStatus status);

    List<Task> findByProjectTeamIdAndProjectStatus(Long teamId, ProjectStatus status);

    List<Task> findByEmployeeIdAndStatusNot(Long employeeId, TaskStatus status);

    void deleteByProjectId(Long projectId);
}
