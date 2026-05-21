package com.backend.management.controller;

import com.backend.management.enums.TaskStatus;
import com.backend.management.enums.ProjectStatus;
import com.backend.management.enums.Role;
import com.backend.management.model.Employee;
import com.backend.management.model.Team;
import com.backend.management.model.Task;
import com.backend.management.model.User;
import com.backend.management.model.Project;
import com.backend.management.repository.EmployeeRepository;
import com.backend.management.repository.TeamRepository;
import com.backend.management.repository.TaskRepository;
import com.backend.management.repository.UserRepository;
import com.backend.management.repository.ProjectRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = {"http://127.0.0.1:4200", "http://localhost:4200"})
public class ProjectController {

    private static final long MINIMUM_PROJECT_BUDGET = 5_000_000L;
    private static final long MINIMUM_DEADLINE_DAYS = 30L;

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;
    private final TaskRepository taskRepository;

    public ProjectController(ProjectRepository projectRepository,
                             UserRepository userRepository,
                             EmployeeRepository employeeRepository,
                             TeamRepository teamRepository,
                             TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.teamRepository = teamRepository;
        this.taskRepository = taskRepository;
    }

    @GetMapping
    public ResponseEntity<?> listProjects(@RequestParam String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "User nem talalhato."));
        }

        return ResponseEntity.ok(projectsForUser(user).stream()
                .map(this::toResponse)
                .toList());
    }

    @GetMapping("/pending")
    public ResponseEntity<?> listPendingProjects(@RequestParam String email) {
        Optional<Employee> reviewer = findActiveProjectReviewer(email);
        if (reviewer.isEmpty()) {
            return ResponseEntity.status(403).body(Map.of("message", "Csak aktiv product owner vagy admin listazhat pending projecteket."));
        }

        return ResponseEntity.ok(projectRepository.findByStatus(ProjectStatus.PENDING).stream()
                .map(this::toResponse)
                .toList());
    }

    @PostMapping
    public ResponseEntity<?> createProject(@RequestParam String email,
                                           @RequestParam(defaultValue = "hu") String language,
                                           @RequestBody CreateProjectRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "User nem talalhato."));
        }

        if (!user.isActive() || (user.getRole() != Role.CUSTOMER && user.getRole() != Role.ADMIN)) {
            return ResponseEntity.status(403).body(Map.of("message",
                    isEnglish(language)
                            ? "Only an active customer or admin can create projects."
                            : "Csak aktiv customer vagy admin hozhat letre projectet."));
        }

        String validationError = validateCreateRequest(request, language);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(Map.of("message", validationError));
        }

        Project project = new Project(
                user,
                request.title().trim(),
                request.description(),
                request.budget(),
                LocalDate.now(),
                request.deadline()
        );

        return ResponseEntity.ok(toResponse(projectRepository.save(project)));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptProject(@PathVariable Long id,
                                           @RequestParam String email,
                                           @RequestParam(defaultValue = "hu") String language) {
        Optional<Employee> reviewer = findActiveProjectReviewer(email);
        if (reviewer.isEmpty()) {
            return ResponseEntity.status(403).body(Map.of("message",
                    isEnglish(language)
                            ? "Only an active product owner or admin can accept projects."
                            : "Csak aktiv product owner vagy admin fogadhat el projectet."));
        }

        return projectRepository.findById(id)
                .map(project -> {
                    if (project.getStatus() != ProjectStatus.PENDING) {
                        return ResponseEntity.badRequest().body(Map.of("message", "Csak PENDING project fogadhato el."));
                    }

                    Employee owner = reviewer.get();
                    Team team = teamRepository.findByOwnerId(owner.getId()).orElse(null);
                    project.setProductOwner(owner);
                    project.setTeam(team);
                    project.setStatus(ProjectStatus.ACCEPTED);

                    return ResponseEntity.ok(toResponse(projectRepository.save(project)));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("message", "Project nem talalhato.")));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectProject(@PathVariable Long id,
                                           @RequestParam String email) {
        Optional<Employee> reviewer = findActiveProjectReviewer(email);
        if (reviewer.isEmpty()) {
            return ResponseEntity.status(403).body(Map.of("message", "Csak aktiv product owner vagy admin utasithat el projectet."));
        }

        return projectRepository.findById(id)
                .map(project -> {
                    if (project.getStatus() != ProjectStatus.PENDING) {
                        return ResponseEntity.badRequest().body(Map.of("message", "Csak PENDING project utasithato el."));
                    }

                    project.setStatus(ProjectStatus.REJECTED);
                    project.setProductOwner(null);
                    project.setTeam(null);

                    return ResponseEntity.ok(toResponse(projectRepository.save(project)));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("message", "Project nem talalhato.")));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeProject(@PathVariable Long id,
                                             @RequestParam String email) {
        Optional<Employee> reviewer = findActiveProjectReviewer(email);
        if (reviewer.isEmpty()) {
            return ResponseEntity.status(403).body(Map.of("message", "Csak aktiv product owner vagy admin zarhat le projectet."));
        }

        return projectRepository.findById(id)
                .map(project -> {
                    Employee owner = reviewer.get();
                    if (project.getProductOwner() == null ||
                            !project.getProductOwner().getId().equals(owner.getId())) {
                        return ResponseEntity.status(403).body(Map.of("message", "Csak a project product ownere zarhatja le."));
                    }

                    if (project.getStatus() != ProjectStatus.ACCEPTED) {
                        return ResponseEntity.badRequest().body(Map.of("message", "Csak ACCEPTED project zarhato le."));
                    }

                    List<Task> tasks = taskRepository.findByProjectId(id);
                    boolean hasOpenTask = tasks.stream()
                            .anyMatch(task -> task.getStatus() != TaskStatus.DONE);
                    if (hasOpenTask) {
                        return ResponseEntity.badRequest().body(Map.of("message", "A project csak minden task DONE statusa utan zarhato le."));
                    }

                    project.setStatus(ProjectStatus.COMPLETED);
                    project.setCompletedAt(LocalDate.now());
                    tasks.stream()
                            .map(Task::getEmployee)
                            .filter(employee -> employee != null)
                            .forEach(employee -> employee.addCompletedTasks(1));

                    return ResponseEntity.ok(toResponse(projectRepository.save(project)));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("message", "Project nem talalhato.")));
    }

    private List<Project> projectsForUser(User user) {
        if (user.getRole() == Role.CUSTOMER) {
            return projectRepository.findByCreatorEmailIgnoreCase(user.getEmail());
        }

        if (user.getRole() == Role.PRODUCT_OWNER) {
            return projectRepository.findByProductOwnerEmailIgnoreCase(user.getEmail());
        }

        return projectRepository.findAll();
    }

    private String validateCreateRequest(CreateProjectRequest request, String language) {
        if (request == null || isBlank(request.title()) || request.deadline() == null) {
            return isEnglish(language)
                    ? "Title and deadline are required."
                    : "Cim es deadline kotelezo.";
        }

        if (request.budget() < MINIMUM_PROJECT_BUDGET) {
            return isEnglish(language)
                    ? "The project budget must be at least 5,000,000 Ft."
                    : "A project koltseg legalabb 5000000 Ft legyen.";
        }

        LocalDate minimumDeadline = LocalDate.now().plusDays(MINIMUM_DEADLINE_DAYS);
        if (request.deadline().isBefore(minimumDeadline)) {
            return isEnglish(language)
                    ? "The deadline must be at least 30 days later."
                    : "A deadline legalabb 30 nappal kesobbi datum legyen.";
        }

        return null;
    }

    private boolean isEnglish(String language) {
        return "en".equalsIgnoreCase(language);
    }

    private Optional<Employee> findActiveProjectReviewer(String email) {
        return employeeRepository.findByEmailIgnoreCase(email)
                .filter(employee -> employee.isActive() &&
                        (employee.getRole() == Role.PRODUCT_OWNER || employee.getRole() == Role.ADMIN));
    }

    private ProjectResponse toResponse(Project project) {
        Employee productOwner = project.getProductOwner();
        Team team = project.getTeam();

        return new ProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getStatus(),
                project.getBudget(),
                project.getStartDate(),
                project.getDeadline(),
                project.getCreator().getEmail(),
                fullName(project.getCreator()),
                productOwner == null ? null : productOwner.getEmail(),
                productOwner == null ? null : fullName(productOwner),
                team == null ? null : team.getName()
        );
    }

    private String fullName(User user) {
        return (user.getLastName() + " " + user.getFirstName()).trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public record CreateProjectRequest(
            String title,
            String description,
            long budget,
            LocalDate deadline
    ) {
    }

    public record ProjectResponse(
            Long id,
            String title,
            String description,
            ProjectStatus status,
            long budget,
            LocalDate startDate,
            LocalDate deadline,
            String creatorEmail,
            String creatorName,
            String productOwnerEmail,
            String productOwnerName,
            String teamName
    ) {
    }
}
