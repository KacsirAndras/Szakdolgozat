package com.backend.management.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
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

import com.backend.management.enums.Role;
import com.backend.management.enums.DayOffStatus;
import com.backend.management.enums.DayOffType;
import com.backend.management.model.Employee;
import com.backend.management.model.User;
import com.backend.management.model.DayOff;
import com.backend.management.repository.EmployeeRepository;
import com.backend.management.repository.DayOffRepository;

@RestController
@RequestMapping("/api/day-off")
@CrossOrigin(origins = {"http://127.0.0.1:4200", "http://localhost:4200"})
public class DayOffController {

    private static final int HOME_OFFICE_LIMIT = 150;

    private final DayOffRepository dayOffRepository;
    private final EmployeeRepository employeeRepository;

    public DayOffController(DayOffRepository dayOffRepository,
                            EmployeeRepository employeeRepository) {
        this.dayOffRepository = dayOffRepository;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    public ResponseEntity<?> listDayOffs(@RequestParam String email) {

        Employee employee = employeeRepository.findByEmailIgnoreCase(email).orElse(null);

        if (employee == null || !employee.isActive() || employee.getRole() == Role.CUSTOMER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Customer nem hasznalhatja a Day off menut."));
        }

        QuotaResponse quotas = quotasForEmployee(employee, LocalDate.now().getYear());

        List<DayOffResponse> myRequests = ownRequests(employee, email)
                .stream()
                .map(this::toResponse)
                .toList();

        List<DayOffResponse> teamRequests = approvableRequests(employee, email)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(new DayOffPageResponse(
                quotas,
                myRequests,
                teamRequests
        ));
    }

    @PostMapping
    public ResponseEntity<?> request(@RequestParam String email, @RequestBody DayOffRequest request) {

        Employee employee = employeeRepository.findByEmailIgnoreCase(email).orElse(null);

        if (employee == null || !employee.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Nem aktiv vagy nem letezo user."));
        }

        if (employee.getRole() == Role.CUSTOMER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Customer nem kerhet dayOffet."));
        }

        if (request == null || request.type() == null || request.startDate() == null || request.endDate() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Type, startDate and endDate are required."));
        }

        if (request.endDate().isBefore(request.startDate())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "A endDate nem lehet korabban, mint a startDate."));
        }

        if (request.startDate().getYear() != request.endDate().getYear()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Egy kerelem csak egy naptari even belul lehet."));
        }

        int workdays = countWorkdays(request.startDate(), request.endDate());

        if (workdays == 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "A kivalasztott idoszakban nincs munkanap."));
        }

        int year = request.startDate().getYear();
        int limit;

        if (request.type() == DayOffType.HOME_OFFICE) {
            limit = HOME_OFFICE_LIMIT;
        } else {
            limit = annualDayOffQuota(employee, year);
        }

        int used = usedDays(employee, request.type(), year);

        if (used + workdays > limit) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message",
                    "Nincs eleg keret. Keret: " + limit + ", mar foglalt: " + used + ", igenyelt: " + workdays + "."
            ));
        }

        boolean overlap = hasOverlap(employee, request.startDate(), request.endDate());

        if (overlap) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erre az idoszakra mar van PENDING vagy APPROVED dayOffed."));
        }

        DayOff dayOff = new DayOff(
                employee,
                null,
                request.type(),
                request.startDate(),
                request.endDate()
        );

        dayOff.setWorkdays(workdays);

        DayOff saved = dayOffRepository.save(dayOff);

        return ResponseEntity.ok(toResponse(saved));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id, @RequestParam String email) {
        return approveOrReject(id, email, DayOffStatus.APPROVED);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestParam String email) {
        return approveOrReject(id, email, DayOffStatus.REJECTED);
    }

    private ResponseEntity<?> approveOrReject(Long id, String email, DayOffStatus status) {

        Employee approver = employeeRepository.findByEmailIgnoreCase(email).orElse(null);

        if (approver == null || !approver.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv user hagyhat jova dayOffet."));
        }

        if (approver.getRole() != Role.ADMIN &&
                approver.getRole() != Role.PRODUCT_OWNER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak ADMIN vagy PRODUCT_OWNER hagyhat jova dayOffet."));
        }

        DayOff dayOff = dayOffRepository.findById(id).orElse(null);

        if (dayOff == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen dayOff."));
        }

        if (dayOff.getStatus() != DayOffStatus.PENDING) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Csak PENDING kerelem kezelheto."));
        }

        if (approver.getRole() != Role.ADMIN) {

            if (dayOff.getEmployee().getRole() != Role.DEVELOPER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Csak DEVELOPER dayOffe hagyhato jova."));
            }

            if (dayOff.getEmployee().getTeam() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "A employeenak nincs teama."));
            }

            if (dayOff.getEmployee().getTeam().getOwner() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "A teamnak nincs owner-je."));
            }

            if (!dayOff.getEmployee().getTeam().getOwner().getId().equals(approver.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Csak a sajat teamod developereinek dayOffet hagyhatod jova."));
            }
        }

        dayOff.setStatus(status);
        dayOff.setApprovedBy(approver);

        DayOff saved = dayOffRepository.save(dayOff);

        return ResponseEntity.ok(toResponse(saved));
    }

    private QuotaResponse quotasForEmployee(Employee employee, int year) {

        int usedHomeOffice = usedDays(employee, DayOffType.HOME_OFFICE, year);
        int dayOffQuota = annualDayOffQuota(employee, year);
        int usedDayOff = usedDays(employee, DayOffType.DAY_OFF, year);

        return new QuotaResponse(
                year,
                HOME_OFFICE_LIMIT,
                usedHomeOffice,
                HOME_OFFICE_LIMIT - usedHomeOffice,
                dayOffQuota,
                usedDayOff,
                dayOffQuota - usedDayOff
        );
    }

    private int usedDays(Employee employee, DayOffType type, int year) {

        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate yearEnd = LocalDate.of(year, 12, 31);

        List<DayOff> dayOffs =
                dayOffRepository.findByEmployeeIdAndTypeAndStatusInAndStartDateBetween(
                        employee.getId(),
                        type,
                        List.of(DayOffStatus.PENDING, DayOffStatus.APPROVED),
                        yearStart,
                        yearEnd
                );

        int total = 0;

        for (DayOff dayOff : dayOffs) {
            total += dayOff.getWorkdays();
        }

        return total;
    }

    private boolean hasOverlap(Employee employee, LocalDate startDate, LocalDate endDate) {

        List<DayOff> dayOffs =
                dayOffRepository.findByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        employee.getId(),
                        List.of(DayOffStatus.PENDING, DayOffStatus.APPROVED),
                        endDate,
                        startDate
                );

        return !dayOffs.isEmpty();
    }

    private List<DayOff> ownRequests(Employee employee, String email) {

        if (employee.getRole() == Role.ADMIN) {
            return dayOffRepository.findAllByOrderByStartDateDesc();
        }

        return dayOffRepository.findByEmployeeEmailIgnoreCaseOrderByStartDateDesc(email);
    }

    private List<DayOff> approvableRequests(Employee employee, String email) {

        if (employee.getRole() == Role.ADMIN) {
            return dayOffRepository.findAllByOrderByStartDateDesc();
        }

        if (employee.getRole() == Role.PRODUCT_OWNER) {

            List<DayOff> all =
                    dayOffRepository.findByEmployeeTeamOwnerEmailIgnoreCaseOrderByStartDateDesc(email);

            return all.stream()
                    .filter(t -> t.getEmployee().getRole() == Role.DEVELOPER)
                    .toList();
        }

        return List.of();
    }

    private int annualDayOffQuota(Employee employee, int year) {

        if (employee.getBirthDate() == null) {
            return 20;
        }

        int age = Period.between(
                employee.getBirthDate(),
                LocalDate.of(year, 12, 31)
        ).getYears();

        int extraDays = 0;

        if (age >= 45) {
            extraDays = 10;
        } else if (age >= 43) {
            extraDays = 9;
        } else if (age >= 41) {
            extraDays = 8;
        } else if (age >= 39) {
            extraDays = 7;
        } else if (age >= 37) {
            extraDays = 6;
        } else if (age >= 35) {
            extraDays = 5;
        } else if (age >= 33) {
            extraDays = 4;
        } else if (age >= 31) {
            extraDays = 3;
        } else if (age >= 28) {
            extraDays = 2;
        } else if (age >= 25) {
            extraDays = 1;
        }

        return 20 + extraDays;
    }

    private int countWorkdays(LocalDate startDate, LocalDate endDate) {

        int days = 0;
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {

            if (current.getDayOfWeek() != DayOfWeek.SATURDAY &&
                    current.getDayOfWeek() != DayOfWeek.SUNDAY) {
                days++;
            }

            current = current.plusDays(1);
        }

        return days;
    }

    private DayOffResponse toResponse(DayOff dayOff) {

        Employee employee = dayOff.getEmployee();
        Employee approvedBy = dayOff.getApprovedBy();

        String approvedByName = null;
        String approvedByEmail = null;

        if (approvedBy != null) {
            approvedByName = fullName(approvedBy);
            approvedByEmail = approvedBy.getEmail();
        }

        return new DayOffResponse(
                dayOff.getId(),
                employee.getId(),
                fullName(employee),
                employee.getEmail(),
                employee.getRole(),
                dayOff.getType(),
                dayOff.getStatus(),
                dayOff.getStartDate(),
                dayOff.getEndDate(),
                dayOff.getWorkdays(),
                approvedByName,
                approvedByEmail
        );
    }

    private String fullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    public record DayOffRequest(
            DayOffType type,
            LocalDate startDate,
            LocalDate endDate
    ) {}

    public record DayOffPageResponse(
            QuotaResponse quotas,
            List<DayOffResponse> myRequests,
            List<DayOffResponse> teamRequests
    ) {}

    public record QuotaResponse(
            int year,
            int homeOfficeLimit,
            int usedHomeOffice,
            int homeOfficeRemaining,
            int dayOffLimit,
            int usedDayOff,
            int dayOffRemaining
    ) {}

    public record DayOffResponse(
            Long id,
            Long employeeId,
            String employeeName,
            String employeeEmail,
            Role employeeRole,
            DayOffType type,
            DayOffStatus status,
            LocalDate startDate,
            LocalDate endDate,
            int workdays,
            String approvedByName,
            String approvedByEmail
    ) {}
}
