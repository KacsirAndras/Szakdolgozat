package com.backend.management.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.management.enums.TaskStatus;
import com.backend.management.enums.Priority;
import com.backend.management.enums.ProjectStatus;
import com.backend.management.enums.Role;
import com.backend.management.model.Employee;
import com.backend.management.model.Task;
import com.backend.management.model.User;
import com.backend.management.model.Project;
import com.backend.management.repository.EmployeeRepository;
import com.backend.management.repository.TaskRepository;
import com.backend.management.repository.ProjectRepository;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = {"http://127.0.0.1:4200", "http://localhost:4200"})
public class TaskController {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;

    public TaskController(TaskRepository taskRepository,
                             ProjectRepository projectRepository,
                             EmployeeRepository employeeRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    public ResponseEntity<?> listTasks(@RequestParam String email) {

        Employee employee = employeeRepository.findByEmailIgnoreCase(email).orElse(null);

        if (employee == null || !employee.isActive()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen aktiv employee."));
        }

        List<Task> tasks;

        if (employee.getRole() == Role.ADMIN) {
            tasks = taskRepository.findAll();
        } else if (employee.getRole() == Role.PRODUCT_OWNER) {
            tasks = taskRepository
                    .findByProjectProductOwnerEmailIgnoreCaseAndProjectStatus(email, ProjectStatus.ACCEPTED);
        } else if (employee.getRole() == Role.DEVELOPER) {
            if (employee.getTeam() == null) {
                return ResponseEntity.ok(List.of());
            }

            tasks = taskRepository
                    .findByProjectTeamIdAndProjectStatus(
                            employee.getTeam().getId(),
                            ProjectStatus.ACCEPTED
                    );
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak ADMIN, PRODUCT_OWNER vagy DEVELOPER hasznalhatja a task menut."));
        }

        List<TaskResponse> response = tasks.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestParam String email, @RequestBody CreateTaskRequest request) {

        Employee owner = employeeRepository.findByEmailIgnoreCase(email).orElse(null);

        if (owner == null || !owner.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Nincs jogosultsagod task letrehozasahoz."));
        }

        if (owner.getRole() != Role.ADMIN &&
                owner.getRole() != Role.PRODUCT_OWNER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak ADMIN vagy PRODUCT_OWNER hozhat letre taskot."));
        }

        if (request == null || isBlank(request.title()) || request.projectId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Project and title are required."));
        }

        Project project = projectRepository.findById(request.projectId()).orElse(null);

        if (project == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen project."));
        }

        if (owner.getRole() != Role.ADMIN) {
            if (project.getProductOwner() == null ||
                    !project.getProductOwner().getId().equals(owner.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Csak a sajat projectedhez hozhatsz letre taskot."));
            }
        }

        if (project.getStatus() != ProjectStatus.ACCEPTED) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Csak ACCEPTED projecthez hozhato letre task."));
        }

        Task task = new Task(
                project,
                request.title().trim(),
                request.description(),
                request.priority()
        );

        Task saved = taskRepository.save(task);

        return ResponseEntity.ok(toResponse(saved));
    }

    @PostMapping("/{id}/take")
    public ResponseEntity<?> takeTask(@PathVariable Long id, @RequestParam String email) {

        Employee developer = employeeRepository.findByEmailIgnoreCase(email).orElse(null);

        if (developer == null || !developer.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy DEVELOPER vehet fel taskot."));
        }

        if (developer.getRole() != Role.ADMIN &&
                developer.getRole() != Role.DEVELOPER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak ADMIN vagy DEVELOPER vehet fel taskot."));
        }

        Task task = taskRepository.findById(id).orElse(null);

        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen task."));
        }

        if (task.getStatus() != TaskStatus.TO_DO) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Csak TO_DO task veheto fel."));
        }

        if (task.getEmployee() != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Ez a task mar valakihez tartozik."));
        }

        if (developer.getRole() != Role.ADMIN) {
            if (!isSameTeam(developer, task.getProject())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Csak a sajat teamod projectjen dolgozhatsz."));
            }
        }

        task.setEmployee(developer);
        task.setStatus(TaskStatus.IN_PROGRESS);

        Task saved = taskRepository.save(task);

        return ResponseEntity.ok(toResponse(saved));
    }

    @PostMapping("/{id}/done")
    public ResponseEntity<?> completeTask(@PathVariable Long id, @RequestParam String email) {

        Employee developer = employeeRepository.findByEmailIgnoreCase(email).orElse(null);

        if (developer == null || !developer.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy DEVELOPER zarhat taskot."));
        }

        if (developer.getRole() != Role.ADMIN &&
                developer.getRole() != Role.DEVELOPER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak ADMIN vagy DEVELOPER zarhat taskot."));
        }

        Task task = taskRepository.findById(id).orElse(null);

        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen task."));
        }

        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Csak IN_PROGRESS task zarhato le."));
        }

        if (task.getEmployee() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "A task nincs senkihez rendelve."));
        }

        if (developer.getRole() != Role.ADMIN) {
            if (!task.getEmployee().getId().equals(developer.getId())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Csak a sajat taskodat zarhatod le."));
            }
        }

        task.setStatus(TaskStatus.DONE);

        Task saved = taskRepository.save(task);

        return ResponseEntity.ok(toResponse(saved));
    }

    private boolean isSameTeam(Employee developer, Project project) {

        if (developer.getTeam() == null) {
            return false;
        }

        if (project.getTeam() == null) {
            return false;
        }

        return developer.getTeam().getId().equals(project.getTeam().getId());
    }

    private TaskResponse toResponse(Task task) {

        Employee employee = task.getEmployee();
        Project project = task.getProject();

        Long employeeId = null;
        String employeeName = null;
        String employeeEmail = null;

        if (employee != null) {
            employeeId = employee.getId();
            employeeName = fullName(employee);
            employeeEmail = employee.getEmail();
        }

        return new TaskResponse(
                task.getId(),
                project.getId(),
                project.getTitle(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                employeeId,
                employeeName,
                employeeEmail
        );
    }

    private String fullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record CreateTaskRequest(
            Long projectId,
            String title,
            String description,
            Priority priority
    ) {}

    public record TaskResponse(
            Long id,
            Long projectId,
            String projectTitle,
            String title,
            String description,
            TaskStatus status,
            Priority priority,
            Long employeeId,
            String employeeName,
            String employeeEmail
    ) {}
}
