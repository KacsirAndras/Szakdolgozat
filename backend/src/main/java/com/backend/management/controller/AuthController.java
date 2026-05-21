package com.backend.management.controller;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

import com.backend.management.enums.Role;
import com.backend.management.model.Customer;
import com.backend.management.model.Employee;
import com.backend.management.model.User;
import com.backend.management.repository.UserRepository;
import com.backend.management.service.EmailService;
import com.backend.management.service.SalaryCalculator;
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://127.0.0.1:4200", "http://localhost:4200"})
public class AuthController {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private static final SecureRandom RANDOM = new SecureRandom();

    public AuthController(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        User user = userRepository.findByEmailIgnoreCase(request.email()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Hibas email vagy password"));
        }

        if (!user.getPassword().equals(request.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Hibas email vagy password"));
        }

        if (!user.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "A user nem aktiv"));
        }

        return ResponseEntity.ok(new LoginResponse(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.isActive()
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {

        User user = userRepository.findByEmailIgnoreCase(request.email()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen user"));
        }

        if (!user.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Nem aktiv user"));
        }

        String code = String.valueOf(100000 + RANDOM.nextInt(900000));
        user.setResetCode(code);
        user.setResetCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        boolean sent = emailService.sendPasswordResetCode(user.getEmail(), code);

        if (!sent) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Email sending failed"));
        }

        return ResponseEntity.ok(Map.of("message", "Kod elkuldve"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {

        if (!request.newPassword().equals(request.newPasswordAgain())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "A ket password nem egyezik"));
        }

        User user = userRepository.findByResetCode(request.token()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Invalid code"));
        }

        if (user.getResetCodeExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(Map.of("message", "Code expired"));
        }

        user.setPassword(request.newPassword());
        user.setResetCode(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password changed"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {

        User user = userRepository.findByEmailIgnoreCase(request.email()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen user"));
        }

        if (!user.getPassword().equals(request.oldPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Rossz regi password"));
        }

        if (!request.newPassword().equals(request.newPasswordAgain())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Nem egyezik az uj password"));
        }

        user.setPassword(request.newPassword());
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PostMapping("/admin-change-password")
    public ResponseEntity<?> adminChangePassword(@RequestBody AdminChangePasswordRequest request) {

        User requester = userRepository.findByEmailIgnoreCase(request.requesterEmail()).orElse(null);

        if (requester == null || !requester.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy IT modosithat masnak passwordot."));
        }

        if (requester.getRole() != Role.ADMIN && requester.getRole() != Role.IT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak ADMIN vagy IT modosithat masnak passwordot."));
        }

        if (isBlank(request.targetEmail()) || isBlank(request.newPassword()) || isBlank(request.newPasswordAgain())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email es uj password mezok kitoltese kotelezo."));
        }

        if (!request.newPassword().equals(request.newPasswordAgain())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Nem egyezik az uj password"));
        }

        User target = userRepository.findByEmailIgnoreCase(request.targetEmail()).orElse(null);

        if (target == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen user"));
        }

        target.setPassword(request.newPassword());
        target.setResetCode(null);
        target.setResetCodeExpiresAt(null);
        userRepository.save(target);

        return ResponseEntity.ok(Map.of("message", "A user passwordje modositva."));
    }

    @PostMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateRequest request) {

        User user = userRepository.findByEmailIgnoreCase(request.currentEmail()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen user"));
        }

        if (isBlank(request.firstName()) ||
                isBlank(request.lastName()) ||
                isBlank(request.email()) ||
                request.birthDate() == null ||
                isBlank(request.phoneNumber()) ||
                isBlank(request.address()) ||
                isBlank(request.city()) ||
                isBlank(request.postalCode()) ||
                isBlank(request.houseNumber())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Minden kotelezo profil mezot ki kell tolteni."));
        }

        User userWithSameEmail = userRepository.findByEmailIgnoreCase(request.email()).orElse(null);
        if (userWithSameEmail != null && !userWithSameEmail.getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Ezzel az email cimmel mar van user."));
        }

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setBirthDate(request.birthDate());
        user.setPhoneNumber(request.phoneNumber());
        user.setAddress(request.address());
        user.setCity(request.city());
        user.setPostalCode(request.postalCode());
        user.setHouseNumber(request.houseNumber());

        if (user instanceof Customer customer) {
            customer.setCompanyName(request.companyName());
        }

        userRepository.save(user);

        return ResponseEntity.ok(new ProfileUpdateResponse(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                "Profil modositva"
        ));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(@RequestParam String email) {

        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen user"));
        }

        String companyName = null;
        String taxId = null;
        String identityCardNumber = null;
        String socialSecurityNumber = null;
        Long salary = null;
        Long netSalary = null;

        if (user instanceof Customer customer) {
            companyName = customer.getCompanyName();
        }

        if (user instanceof Employee employee) {
            taxId = employee.getTaxId();
            identityCardNumber = employee.getIdentityCardNumber();
            socialSecurityNumber = employee.getSocialSecurityNumber();
            salary = employee.getSalary();
            netSalary = SalaryCalculator.netSalary(employee.getSalary(), employee.getHireDate(), LocalDate.now());
        }

        return ResponseEntity.ok(new ProfileResponse(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getBirthDate(),
                user.getPhoneNumber(),
                user.getPostalCode(),
                user.getCity(),
                user.getAddress(),
                user.getHouseNumber(),
                user.getRole(),
                companyName,
                taxId,
                identityCardNumber,
                socialSecurityNumber,
                salary,
                netSalary
        ));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record LoginRequest(String email, String password) {}
    public record LoginResponse(String email, String firstName, String lastName, Role role, boolean active) {}
    public record ForgotPasswordRequest(String email) {}
    public record ResetPasswordRequest(
            String token,
            String newPassword,
            String newPasswordAgain
    ) {}
    public record ChangePasswordRequest(
            String email,
            String oldPassword,
            String newPassword,
            String newPasswordAgain
    ) {}
    public record AdminChangePasswordRequest(
            String requesterEmail,
            String targetEmail,
            String newPassword,
            String newPasswordAgain
    ) {}
    public record ProfileUpdateRequest(
            String currentEmail,
            String firstName,
            String lastName,
            String email,
            LocalDate birthDate,
            String phoneNumber,
            String address,
            String city,
            String postalCode,
            String houseNumber,
            String companyName
    ) {}
    public record ProfileUpdateResponse(String email, String firstName, String lastName, String message) {}
    public record ProfileResponse(
            String firstName,
            String lastName,
            String email,
            LocalDate birthDate,
            String phoneNumber,
            String postalCode,
            String city,
            String address,
            String houseNumber,
            Role role,
            String companyName,
            String taxId,
            String identityCardNumber,
            String socialSecurityNumber,
            Long salary,
            Long netSalary
    ) {}
}
