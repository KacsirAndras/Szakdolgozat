package com.backend.management.controller;

import com.backend.management.enums.ProjectStatus;
import com.backend.management.enums.Role;
import com.backend.management.model.Employee;
import com.backend.management.model.User;
import com.backend.management.model.Project;
import com.backend.management.repository.EmployeeRepository;
import com.backend.management.repository.UserRepository;
import com.backend.management.repository.ProjectRepository;
import com.backend.management.service.SalaryCalculator;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = {"http://127.0.0.1:4200", "http://localhost:4200"})
public class StatisticsController {

    private static final LocalDate START_DATE = LocalDate.of(2020, 1, 1);
    private static final long START_BALANCE = 20_000_000L;

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;

    public StatisticsController(UserRepository userRepository,
                                EmployeeRepository employeeRepository,
                                ProjectRepository projectRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
    }

    @GetMapping
    public ResponseEntity<?> getStatistics(@RequestParam String email,
                                           @RequestParam(required = false) Integer year) {

        if (!isActiveAdmin(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN lathatja a statisztikat."));
        }

        int currentYear = LocalDate.now().getYear();
        int selectedYear = year == null ? currentYear : year;

        if (selectedYear < 2000) {
            selectedYear = 2000;
        }

        if (selectedYear > currentYear) {
            selectedYear = currentYear;
        }

        List<User> users = userRepository.findAll();
        List<Employee> employees = employeeRepository.findAll();
        List<Project> projects = projectRepository.findAll();

        long monthlySalaryCost = 0;

        for (Employee employee : employees) {
            if (employee.isActive()) {
                monthlySalaryCost += SalaryCalculator.netSalary(
                        employee.getSalary(),
                        employee.getHireDate(),
                        LocalDate.now()
                );
            }
        }

        List<RoleSlice> roleSlices = new ArrayList<>();

        for (Role role : Role.values()) {
            long count = 0;

            for (User user : users) {
                if (user.getRole() == role) {
                    count++;
                }
            }

            roleSlices.add(new RoleSlice(
                    role.name(),
                    count,
                    colorForRole(role)
            ));
        }

        YearMonth firstMonth = YearMonth.of(selectedYear, 1);
        long balanceBeforeYear = calculateBalanceUntil(firstMonth, monthlySalaryCost, projects);

        long balance = balanceBeforeYear;
        List<MonthlyFinance> months = new ArrayList<>();

        for (Month month : Month.values()) {

            YearMonth currentMonth = YearMonth.of(selectedYear, month);

            long projectRevenue = completedProjectRevenue(projects, currentMonth);
            long profit = projectRevenue - monthlySalaryCost;

            balance = balance + profit;

            months.add(new MonthlyFinance(
                    month.getValue(),
                    hungarianMonthName(month),
                    projectRevenue,
                    monthlySalaryCost,
                    profit,
                    balance
            ));
        }

        StatisticsResponse response = new StatisticsResponse(
                START_DATE,
                START_BALANCE,
                selectedYear,
                balanceBeforeYear,
                roleSlices,
                months
        );

        return ResponseEntity.ok(response);
    }

    private long calculateBalanceUntil(YearMonth targetMonth,
                                       long monthlySalaryCost,
                                       List<Project> projects) {

        long balance = START_BALANCE;

        YearMonth current = YearMonth.from(START_DATE);

        while (current.isBefore(targetMonth)) {

            long revenue = completedProjectRevenue(projects, current);
            long profit = revenue - monthlySalaryCost;

            balance = balance + profit;

            current = current.plusMonths(1);
        }

        return balance;
    }

    private long completedProjectRevenue(List<Project> projects, YearMonth month) {

        long total = 0;

        for (Project project : projects) {

            if (project.getStatus() == ProjectStatus.COMPLETED &&
                    project.getCompletedAt() != null) {

                YearMonth projectMonth = YearMonth.from(project.getCompletedAt());

                if (projectMonth.equals(month)) {
                    total += project.getBudget();
                }
            }
        }

        return total;
    }

    private boolean isActiveAdmin(String email) {

        if (email == null || email.isBlank()) {
            return false;
        }

        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user == null) {
            return false;
        }

        if (!user.isActive()) {
            return false;
        }

        return user.getRole() == Role.ADMIN;
    }

    private String colorForRole(Role role) {

        if (role == Role.CUSTOMER) {
            return "#0969da";
        }

        if (role == Role.ADMIN) {
            return "#a40e26";
        }

        if (role == Role.IT) {
            return "#1a7f37";
        }

        if (role == Role.HR) {
            return "#bf8700";
        }

        if (role == Role.PRODUCT_OWNER) {
            return "#8250df";
        }

        if (role == Role.DEVELOPER) {
            return "#57606a";
        }

        return "#000000";
    }

    private String hungarianMonthName(Month month) {

        if (month == Month.JANUARY) {
            return "Januar";
        } else if (month == Month.FEBRUARY) {
            return "Februar";
        } else if (month == Month.MARCH) {
            return "Marcius";
        } else if (month == Month.APRIL) {
            return "Aprilis";
        } else if (month == Month.MAY) {
            return "Majus";
        } else if (month == Month.JUNE) {
            return "Junius";
        } else if (month == Month.JULY) {
            return "Julius";
        } else if (month == Month.AUGUST) {
            return "Augusztus";
        } else if (month == Month.SEPTEMBER) {
            return "Szeptember";
        } else if (month == Month.OCTOBER) {
            return "Oktober";
        } else if (month == Month.NOVEMBER) {
            return "November";
        } else if (month == Month.DECEMBER) {
            return "December";
        }

        return "";
    }

    public record StatisticsResponse(
            LocalDate startDate,
            long startBalance,
            int year,
            long balanceBeforeYear,
            List<RoleSlice> roleSlices,
            List<MonthlyFinance> months
    ) {}

    public record RoleSlice(
            String role,
            long count,
            String color
    ) {}

    public record MonthlyFinance(
            int month,
            String label,
            long projectIncome,
            long salaryCost,
            long profit,
            long balanceAfter
    ) {}
}
