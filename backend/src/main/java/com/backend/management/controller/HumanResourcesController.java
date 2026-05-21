package com.backend.management.controller;

import com.backend.management.enums.TaskStatus;
import com.backend.management.enums.ProjectStatus;
import com.backend.management.enums.Role;
import com.backend.management.model.Employee;
import com.backend.management.repository.EmployeeRepository;
import com.backend.management.repository.TeamRepository;
import com.backend.management.repository.TaskRepository;
import com.backend.management.repository.ProjectRepository;
import com.backend.management.service.SalaryCalculator;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hr")
@CrossOrigin(origins = {"http://127.0.0.1:4200", "http://localhost:4200"})
public class HumanResourcesController {
    private static final long MINIMUM_GROSS_SALARY = 322_800L;

    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;

    public HumanResourcesController(EmployeeRepository employeeRepository,
                                    TaskRepository taskRepository,
                                    ProjectRepository projectRepository,
                                    TeamRepository teamRepository) {
        this.employeeRepository = employeeRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.teamRepository = teamRepository;
    }

    @GetMapping("/employees")
    public ResponseEntity<?> listEmployees(@RequestParam String requesterEmail) {
        if (!canUseHumanResources(requesterEmail)) {
            return forbidden();
        }

        List<EmployeeResponse> employees = employeeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(employees);
    }

    @PostMapping("/employees")
    public ResponseEntity<?> createEmployee(@RequestParam String requesterEmail,
                                            @RequestParam(defaultValue = "hu") String language,
                                            @RequestBody EmployeeRequest request) {
        if (!canUseHumanResources(requesterEmail)) {
            return forbidden();
        }

        if (isBlank(request.email()) || isBlank(request.password()) || isBlank(request.firstName()) ||
                isBlank(request.lastName()) || request.role() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Keresztname, lastName, email, password es role kotelezo."));
        }

        if (employeeRepository.findByEmailIgnoreCase(request.email()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Ezzel az email cimmel mar van employee."));
        }

        String validationError = validateEmployeeRequest(request, language);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(Map.of("message", validationError));
        }

        Employee employee = new Employee(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.password(),
                request.birthDate(),
                request.phoneNumber(),
                request.address(),
                request.city(),
                request.postalCode(),
                request.houseNumber(),
                request.role(),
                request.active(),
                request.taxId(),
                request.identityCardNumber(),
                request.socialSecurityNumber(),
                request.salary()
        );
        employee.setHireDate(LocalDate.now());

        return ResponseEntity.ok(toResponse(employeeRepository.save(employee)));
    }

    @PutMapping("/employees/{id}")
    @Transactional
    public ResponseEntity<?> updateEmployee(@RequestParam String requesterEmail,
                                            @RequestParam(defaultValue = "hu") String language,
                                            @PathVariable Long id,
                                            @RequestBody EmployeeRequest request) {
        if (!canUseHumanResources(requesterEmail)) {
            return forbidden();
        }

        return employeeRepository.findById(id)
                .<ResponseEntity<?>>map(employee -> {
                    Role previousRole = employee.getRole();
                    if (isBlank(request.email()) || isBlank(request.firstName()) ||
                            isBlank(request.lastName()) || request.role() == null) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message", "Keresztname, lastName, email es role kotelezo."));
                    }

                    var employeeWithSameEmail = employeeRepository.findByEmailIgnoreCase(request.email());
                    if (employeeWithSameEmail.isPresent() && !employeeWithSameEmail.get().getId().equals(id)) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message", "Ezzel az email cimmel mar van employee."));
                    }

                    String validationError = validateEmployeeRequest(request, language);
                    if (validationError != null) {
                        return ResponseEntity.badRequest().body(Map.of("message", validationError));
                    }

                    employee.setFirstName(request.firstName());
                    employee.setLastName(request.lastName());
                    employee.setEmail(request.email());
                    if (!isBlank(request.password())) {
                        employee.setPassword(request.password());
                    }
                    employee.setBirthDate(request.birthDate());
                    employee.setPhoneNumber(request.phoneNumber());
                    employee.setAddress(request.address());
                    employee.setCity(request.city());
                    employee.setPostalCode(request.postalCode());
                    employee.setHouseNumber(request.houseNumber());
                    employee.setRole(request.role());
                    employee.setActive(request.active());
                    employee.setTaxId(request.taxId());
                    employee.setIdentityCardNumber(request.identityCardNumber());
                    employee.setSocialSecurityNumber(request.socialSecurityNumber());
                    employee.setSalary(request.salary());

                    if (!request.active() || previousRole != request.role()) {
                        removeEmployeeFromRole(employee, previousRole);
                    }

                    return ResponseEntity.ok(toResponse(employeeRepository.save(employee)));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Nincs ilyen employee.")));
    }

    @PostMapping("/employees/{id}/deactivate")
    @Transactional
    public ResponseEntity<?> deactivateEmployee(@RequestParam String requesterEmail, @PathVariable Long id) {
        if (!canUseHumanResources(requesterEmail)) {
            return forbidden();
        }

        return employeeRepository.findById(id)
                .<ResponseEntity<?>>map(employee -> {
                    employee.setActive(false);
                    removeEmployeeFromRole(employee, employee.getRole());
                    return ResponseEntity.ok(toResponse(employeeRepository.save(employee)));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Nincs ilyen employee.")));
    }

    private boolean canUseHumanResources(String email) {
        if (isBlank(email)) {
            return false;
        }

        return employeeRepository.findByEmailIgnoreCase(email)
                .filter(Employee::isActive)
                .filter(employee -> employee.getRole() == Role.ADMIN ||
                        employee.getRole() == Role.HR)
                .isPresent();
    }

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Csak aktiv ADMIN es HR hasznalhatja a Human resources menut."));
    }

    private EmployeeResponse toResponse(Employee employee) {
        Long teamId = employee.getTeam() == null ? null : employee.getTeam().getId();
        String teamName = employee.getTeam() == null ? null : employee.getTeam().getName();

        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getBirthDate(),
                employee.getPhoneNumber(),
                employee.getAddress(),
                employee.getCity(),
                employee.getPostalCode(),
                employee.getHouseNumber(),
                employee.getRole(),
                employee.isActive(),
                employee.getTaxId(),
                employee.getIdentityCardNumber(),
                employee.getSocialSecurityNumber(),
                employee.getSalary(),
                adjustedGrossSalary(employee),
                netSalary(employee),
                employee.getHireDate(),
                teamId,
                teamName,
                employee.getCompletedTasks(),
                employee.getClosedTickets()
        );
    }

    private void removeEmployeeFromRole(Employee employee, Role removedRole) {
        if (removedRole == Role.DEVELOPER) {
            taskRepository.findByEmployeeIdAndStatusNot(employee.getId(), TaskStatus.DONE)
                    .forEach(task -> {
                        task.setEmployee(null);
                        task.setStatus(TaskStatus.TO_DO);
                        taskRepository.save(task);
                    });
            employee.setTeam(null);
        }

        if (removedRole == Role.PRODUCT_OWNER) {
            projectRepository.findByProductOwnerIdAndStatus(employee.getId(), ProjectStatus.ACCEPTED)
                    .forEach(project -> {
                        taskRepository.deleteByProjectId(project.getId());
                        project.setProductOwner(null);
                        project.setTeam(null);
                        project.setStatus(ProjectStatus.PENDING);
                        projectRepository.save(project);
                    });

            teamRepository.findByOwnerId(employee.getId()).ifPresent(team -> {
                team.getMembers().forEach(member -> {
                    member.setTeam(null);
                    employeeRepository.save(member);
                });
                teamRepository.delete(team);
            });
        }
    }

    private String validateEmployeeRequest(EmployeeRequest request, String language) {
        if (isBlank(request.firstName()) ||
                isBlank(request.lastName()) ||
                isBlank(request.email()) ||
                request.birthDate() == null ||
                isBlank(request.phoneNumber()) ||
                isBlank(request.address()) ||
                isBlank(request.city()) ||
                isBlank(request.postalCode()) ||
                isBlank(request.houseNumber()) ||
                request.role() == null ||
                request.salary() == null) {
            return "Minden kotelezo alkalmazotti mezot ki kell tolteni.";
        }

        if (request.role() == Role.ADMIN) {
            return "Human resources feluleten nem adhato ADMIN role.";
        }

        if (!isValidEmail(request.email())) {
            return "Adj meg egy ervenyes email cimet.";
        }

        if (!isValidPhoneNumber(request.phoneNumber())) {
            return "A telefonszam 8-15 szamjegy legyen, opcionalis + elotaggal.";
        }

        if (!isValidPostalCode(request.postalCode())) {
            return "Az iranyitoszam pontosan 4 szamjegy legyen.";
        }

        if (!request.houseNumber().trim().matches("^\\d+$")) {
            return "A hazszam csak szam lehet.";
        }

        int age = Period.between(request.birthDate(), LocalDate.now()).getYears();
        if (age < 18) {
            return isEnglish(language)
                    ? "Employees under 18 cannot be added."
                    : "18 ev alatti alkalmazott nem veheto fel.";
        }

        if (request.salary() < MINIMUM_GROSS_SALARY) {
            return isEnglish(language)
                    ? "The salary cannot be lower than the minimum wage."
                    : "A fizetes nem lehet kisebb a minimalbernel.";
        }

        return null;
    }

    private boolean isEnglish(String language) {
        return "en".equalsIgnoreCase(language);
    }

    private boolean isValidEmail(String value) {
        return value != null && value.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    private boolean isValidPhoneNumber(String value) {
        return value != null && value.trim().matches("^\\+?\\d{8,15}$");
    }

    private boolean isValidPostalCode(String value) {
        return value != null && value.trim().matches("^\\d{4}$");
    }

    private long adjustedGrossSalary(Employee employee) {
        return SalaryCalculator.adjustedGrossSalary(employee.getSalary(), employee.getHireDate(), LocalDate.now());
    }

    private long netSalary(Employee employee) {
        return SalaryCalculator.netSalary(employee.getSalary(), employee.getHireDate(), LocalDate.now());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record EmployeeRequest(
            String firstName,
            String lastName,
            String email,
            String password,
            LocalDate birthDate,
            String phoneNumber,
            String address,
            String city,
            String postalCode,
            String houseNumber,
            Role role,
            boolean active,
            String taxId,
            String identityCardNumber,
            String socialSecurityNumber,
            Long salary
    ) {}

    public record EmployeeResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            LocalDate birthDate,
            String phoneNumber,
            String address,
            String city,
            String postalCode,
            String houseNumber,
            Role role,
            boolean active,
            String taxId,
            String identityCardNumber,
            String socialSecurityNumber,
            Long salary,
            long adjustedGrossSalary,
            long netSalary,
            LocalDate hireDate,
            Long teamId,
            String teamName,
            int completedTasks,
            int closedTickets
    ) {}
}
