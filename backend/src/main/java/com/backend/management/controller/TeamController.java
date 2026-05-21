package com.backend.management.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.management.enums.TaskStatus;
import com.backend.management.enums.Role;
import com.backend.management.model.Employee;
import com.backend.management.model.Project;
import com.backend.management.model.Team;
import com.backend.management.repository.EmployeeRepository;
import com.backend.management.repository.ProjectRepository;
import com.backend.management.repository.TeamRepository;
import com.backend.management.repository.TaskRepository;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = {"http://127.0.0.1:4200", "http://localhost:4200"})
public class TeamController {

    private final TeamRepository teamRepository;
    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TeamController(TeamRepository teamRepository,
                          EmployeeRepository employeeRepository,
                          TaskRepository taskRepository,
                          ProjectRepository projectRepository) {
        this.teamRepository = teamRepository;
        this.employeeRepository = employeeRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyTeam(@RequestParam String ownerEmail) {

        Employee owner = employeeRepository.findByEmailIgnoreCase(ownerEmail).orElse(null);

        if (!canManageOwnedTeam(owner)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy PRODUCT_OWNER kerheti le a teamot."));
        }

        Team team = teamRepository.findByOwnerEmailIgnoreCase(owner.getEmail()).orElse(null);

        if (team == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Meg nincs teamod."));
        }

        return ResponseEntity.ok(toTeamResponse(team));
    }

    @GetMapping
    public ResponseEntity<?> getTeams(@RequestParam String email) {

        Employee user = employeeRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user == null || !user.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy DEVELOPER lathatja az osszes teamot."));
        }

        if (user.getRole() != Role.ADMIN &&
                user.getRole() != Role.DEVELOPER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy DEVELOPER lathatja az osszes teamot."));
        }

        List<TeamResponse> teams = teamRepository.findAll()
                .stream()
                .map(this::toTeamResponse)
                .toList();

        return ResponseEntity.ok(teams);
    }

    @GetMapping("/membership")
    public ResponseEntity<?> getMembership(@RequestParam String email) {

        Employee user = employeeRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user == null || !user.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy DEVELOPER kerheti le a sajat teamat."));
        }

        if (user.getRole() != Role.ADMIN &&
                user.getRole() != Role.DEVELOPER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy DEVELOPER kerheti le a sajat teamat."));
        }

        if (user.getTeam() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Meg nem vagy teamban."));
        }

        return ResponseEntity.ok(toTeamResponse(user.getTeam()));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTeam(@RequestBody CreateTeamRequest request) {

        if (request == null || request.ownerEmail() == null || request.ownerEmail().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "owner email kotelezo."));
        }

        Employee owner = employeeRepository.findByEmailIgnoreCase(request.ownerEmail()).orElse(null);

        if (!canManageOwnedTeam(owner)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy PRODUCT_OWNER hozhat letre teamot."));
        }

        if (teamRepository.findByOwnerEmailIgnoreCase(owner.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Ennek a team ownernek mar van teama."));
        }

        Team team = new Team(request.name(), owner);
        Team saved = teamRepository.save(team);

        return ResponseEntity.ok(toTeamResponse(saved));
    }

    @GetMapping("/available-developers")
    public ResponseEntity<?> getAvailableDevelopers(@RequestParam String ownerEmail) {

        Employee owner = employeeRepository.findByEmailIgnoreCase(ownerEmail).orElse(null);

        if (!canManageOwnedTeam(owner)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy PRODUCT_OWNER lathatja a szabad DEVELOPERket."));
        }

        List<EmployeeResponse> developers = employeeRepository
                .findByRoleAndActiveTrueAndTeamIsNull(Role.DEVELOPER)
                .stream()
                .map(this::toEmployeeResponse)
                .toList();

        return ResponseEntity.ok(developers);
    }

    @PostMapping("/add-member")
    public ResponseEntity<?> addMember(@RequestBody AddMemberRequest request) {

        if (request == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Hianyzo adatok."));
        }

        Employee owner = employeeRepository.findByEmailIgnoreCase(request.ownerEmail()).orElse(null);

        if (!canManageOwnedTeam(owner)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy PRODUCT_OWNER vehet fel DEVELOPERt."));
        }

        Team team = teamRepository.findByOwnerEmailIgnoreCase(owner.getEmail()).orElse(null);

        if (team == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Eloszor hozz letre teamot."));
        }

        Employee developer = employeeRepository.findById(request.developerId()).orElse(null);

        if (developer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen DEVELOPER."));
        }

        if (!developer.isActive() || developer.getRole() != Role.DEVELOPER) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Csak aktiv DEVELOPER veheto fel teamba."));
        }

        if (developer.getTeam() != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Ez a DEVELOPER mar teamban van."));
        }

        developer.setTeam(team);
        employeeRepository.save(developer);

        Team updated = teamRepository.findById(team.getId()).orElse(team);

        return ResponseEntity.ok(toTeamResponse(updated));
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteTeam(@RequestBody DeleteTeamRequest request) {

        if (request == null || request.requesterEmail() == null || request.requesterEmail().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "requester email kotelezo."));
        }

        Employee requester = employeeRepository.findByEmailIgnoreCase(request.requesterEmail()).orElse(null);

        if (!canManageOwnedTeam(requester)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy PRODUCT_OWNER szuntethet meg teamot."));
        }

        Team team = teamRepository.findByOwnerEmailIgnoreCase(requester.getEmail()).orElse(null);

        if (team == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Meg nincs teamod."));
        }

        detachTeam(team);
        teamRepository.delete(team);

        return ResponseEntity.ok(Map.of("message", "A team megszuntetve."));
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinTeam(@RequestBody JoinTeamRequest request) {

        if (request == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Hianyzo adatok."));
        }

        Employee user = employeeRepository.findByEmailIgnoreCase(request.email()).orElse(null);

        if (user == null || !user.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy DEVELOPER csatlakozhat teamhoz."));
        }

        if (user.getRole() != Role.ADMIN &&
                user.getRole() != Role.DEVELOPER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy DEVELOPER csatlakozhat teamhoz."));
        }

        Team team = teamRepository.findById(request.teamId()).orElse(null);

        if (team == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen team."));
        }

        if (user.getTeam() != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Mar tagja vagy egy teamnak. Eloszor lepj ki onnan."));
        }

        user.setTeam(team);
        employeeRepository.save(user);

        Team updated = teamRepository.findById(team.getId()).orElse(team);

        return ResponseEntity.ok(toTeamResponse(updated));
    }

    @PostMapping("/leave")
    public ResponseEntity<?> leaveTeam(@RequestBody LeaveTeamRequest request) {

        if (request == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Hianyzo adatok."));
        }

        Employee user = employeeRepository.findByEmailIgnoreCase(request.email()).orElse(null);

        if (user == null || !user.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy DEVELOPER lephet ki teambol."));
        }

        if (user.getRole() != Role.ADMIN &&
                user.getRole() != Role.DEVELOPER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy DEVELOPER lephet ki teambol."));
        }

        if (user.getTeam() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Nem vagy teamban."));
        }

        user.setTeam(null);

        releaseOpenTasks(user);

        employeeRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Sirequesten kileptel a teambol."));
    }

    private void releaseOpenTasks(Employee developer) {

        taskRepository.findByEmployeeIdAndStatusNot(
                developer.getId(),
                TaskStatus.DONE
        ).forEach(task -> {
            task.setEmployee(null);
            task.setStatus(TaskStatus.TO_DO);
            taskRepository.save(task);
        });
    }

    private boolean canManageOwnedTeam(Employee employee) {
        return employee != null &&
                employee.isActive() &&
                (employee.getRole() == Role.ADMIN || employee.getRole() == Role.PRODUCT_OWNER);
    }

    private void detachTeam(Team team) {

        for (Employee member : List.copyOf(team.getMembers())) {
            member.setTeam(null);
            releaseOpenTasks(member);
            employeeRepository.save(member);
        }

        for (Project project : projectRepository.findByTeamId(team.getId())) {
            project.setTeam(null);
            projectRepository.save(project);
        }
    }

    private TeamResponse toTeamResponse(Team team) {

        List<EmployeeResponse> members = team.getMembers()
                .stream()
                .map(this::toEmployeeResponse)
                .toList();

        return new TeamResponse(
                team.getId(),
                team.getName(),
                toEmployeeResponse(team.getOwner()),
                members
        );
    }

    private EmployeeResponse toEmployeeResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getRole().name()
        );
    }

    public record CreateTeamRequest(
            String ownerEmail,
            String name
    ) {}

    public record AddMemberRequest(
            String ownerEmail,
            Long developerId
    ) {}

    public record JoinTeamRequest(
            String email,
            Long teamId
    ) {}

    public record LeaveTeamRequest(
            String email
    ) {}

    public record DeleteTeamRequest(
            String requesterEmail
    ) {}

    public record EmployeeResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            String role
    ) {}

    public record TeamResponse(
            Long id,
            String name,
            EmployeeResponse owner,
            List<EmployeeResponse> members
    ) {}
}
