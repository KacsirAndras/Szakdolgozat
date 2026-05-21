package com.backend.management.model;

import com.backend.management.enums.TaskStatus;
import com.backend.management.enums.TicketStatus;
import com.backend.management.enums.TicketType;
import com.backend.management.enums.Priority;
import com.backend.management.enums.ProjectStatus;
import com.backend.management.enums.Role;
import com.backend.management.enums.DayOffStatus;
import com.backend.management.enums.DayOffType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DomainModelTests {

    @Test
    void projectStartsAsPendingWithGivenDatesAndCost() {
        User customer = new User("Customer", "Elek", "customer@example.com", "pw",
                null, null, null, null, null, null, Role.CUSTOMER, true);

        Project project = new Project(customer, "Portal", "Demo", 5_000_000L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.PENDING);
        assertThat(project.getCreator()).isEqualTo(customer);
        assertThat(project.getBudget()).isEqualTo(5_000_000L);
        assertThat(project.getCompletedAt()).isNull();
    }

    @Test
    void taskDefaultsToTodoAndMediumPriorityWhenPriorityIsMissing() {
        Project project = new Project();

        Task task = new Task(project, "Teszt task", "Leiras", null);

        assertThat(task.getProject()).isEqualTo(project);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.TO_DO);
        assertThat(task.getPriority()).isEqualTo(Priority.MEDIUM);
    }

    @Test
    void ticketStartsClosedForCreatedSupportRequest() {
        User creator = new User("Dev", "Dora", "dev@example.com", "pw",
                null, null, null, null, null, null, Role.DEVELOPER, true);

        Ticket ticket = new Ticket(creator, TicketType.HELP, "Nem mukodik");

        assertThat(ticket.getCreatedBy()).isEqualTo(creator);
        assertThat(ticket.getType()).isEqualTo(TicketType.HELP);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CLOSED);
    }

    @Test
    void dayOffStartsPendingAndKeepsWorkingDays() {
        Employee employee = new Employee();
        DayOff dayOff = new DayOff(employee, null, DayOffType.DAY_OFF,
                LocalDate.of(2026, 5, 4), LocalDate.of(2026, 5, 8));

        dayOff.setWorkdays(5);

        assertThat(dayOff.getEmployee()).isEqualTo(employee);
        assertThat(dayOff.getStatus()).isEqualTo(DayOffStatus.PENDING);
        assertThat(dayOff.getWorkdays()).isEqualTo(5);
    }

    @Test
    void employeeCountersCanBeIncremented() {
        Employee employee = new Employee();

        employee.addCompletedTasks(3);
        employee.addClosedTicket();

        assertThat(employee.getCompletedTasks()).isEqualTo(3);
        assertThat(employee.getClosedTickets()).isEqualTo(1);
    }
}
