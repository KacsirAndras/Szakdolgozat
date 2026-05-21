package com.backend.management.controller;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.management.enums.Role;
import com.backend.management.model.Customer;
import com.backend.management.model.User;
import com.backend.management.repository.CustomerRepository;
import com.backend.management.repository.UserRepository;
import com.backend.management.service.EmailService;
@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = {"http://127.0.0.1:4200", "http://localhost:4200"})
public class CustomerController {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public CustomerController(CustomerRepository customerRepository,
                            UserRepository userRepository,
                            EmailService emailService) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam(defaultValue = "hu") String language,
                                      @RequestBody CustomerRegistrationRequest request) {

        if (request == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Minden mezot ki kell tolteni."));
        }

        if (isBlank(request.firstName()) ||
                isBlank(request.lastName()) ||
                request.birthDate() == null ||
                isBlank(request.email()) ||
                isBlank(request.phoneNumber()) ||
                isBlank(request.address()) ||
                isBlank(request.city()) ||
                isBlank(request.postalCode()) ||
                isBlank(request.houseNumber()) ||
                isBlank(request.password()) ||
                isBlank(request.passwordAgain())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Minden mezot ki kell tolteni."));
        }

        if (!isValidPhoneNumber(request.phoneNumber())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "A telefonszam 8-15 szamjegy legyen, opcionalis + elotaggal."));
        }

        if (!isValidPostalCode(request.postalCode())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Az iranyitoszam pontosan 4 szamjegy legyen."));
        }

        int age = Period.between(request.birthDate(), LocalDate.now()).getYears();

        if (age < 18) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Customers under 18 cannot register."));
        }

        User existingUser = userRepository.findByEmailIgnoreCase(request.email()).orElse(null);

        if (existingUser != null) {
            if (existingUser instanceof Customer existingCustomer
                    && existingCustomer.getRole() == Role.CUSTOMER
                    && !existingCustomer.isActive()) {
                return updateInactiveCustomerRegistration(existingCustomer, request, language);
            }

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Ezzel az email cimmel mar van user."));
        }

        if (!request.password().equals(request.passwordAgain())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "A ket password nem egyezik."));
        }

        String companyName = normalizeCompanyName(request.companyName());

        if (companyName != null && !isTitleCase(companyName)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "A ceg name minden szava nagybetuvel kezdodjon."));
        }

        Customer customer = new Customer(
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
                Role.CUSTOMER,
                false,
                companyName
        );

        String activationCode = createCode();

        customer.setActivationCode(activationCode);
        customer.setActivationCodeExpiresAt(LocalDateTime.now().plusMinutes(30));

        Customer saved = customerRepository.save(customer);

        boolean emailSent = emailService.sendActivationCode(saved.getEmail(), activationCode, language);

        String message;

        if (emailSent) {
            message = "Registration succeeded. The activation code was sent by email.";
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Registration succeeded, but the activation email could not be sent. Check SMTP settings."));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", saved.getId(),
                "email", saved.getEmail(),
                "role", saved.getRole(),
                "active", saved.isActive(),
                "message", message
        ));
    }

    private ResponseEntity<?> updateInactiveCustomerRegistration(Customer customer,
                                                                CustomerRegistrationRequest request,
                                                                String language) {

        if (!request.password().equals(request.passwordAgain())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "A ket password nem egyezik."));
        }

        String companyName = normalizeCompanyName(request.companyName());

        if (companyName != null && !isTitleCase(companyName)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "A ceg name minden szava nagybetuvel kezdodjon."));
        }

        String activationCode = createCode();

        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setPassword(request.password());
        customer.setBirthDate(request.birthDate());
        customer.setPhoneNumber(request.phoneNumber());
        customer.setAddress(request.address());
        customer.setCity(request.city());
        customer.setPostalCode(request.postalCode());
        customer.setHouseNumber(request.houseNumber());
        customer.setCompanyName(companyName);
        customer.setActivationCode(activationCode);
        customer.setActivationCodeExpiresAt(LocalDateTime.now().plusMinutes(30));

        Customer saved = customerRepository.save(customer);
        boolean emailSent = emailService.sendActivationCode(saved.getEmail(), activationCode, language);

        String message;

        if (emailSent) {
            message = "There was already a pending registration for this email address. We sent a new activation code.";
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "The activation email could not be sent. Check SMTP settings."));
        }

        return ResponseEntity.ok(Map.of(
                "id", saved.getId(),
                "email", saved.getEmail(),
                "role", saved.getRole(),
                "active", saved.isActive(),
                "message", message
        ));
    }

    @PostMapping("/activate")
    public ResponseEntity<?> activate(@RequestBody ActivationRequest request) {

        if (request == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email address and activation code are required."));
        }

        if (isBlank(request.email()) || isBlank(request.token())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email address and activation code are required."));
        }

        User user = userRepository.findByActivationCode(request.token()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Invalid activation code."));
        }

        if (!user.getEmail().equalsIgnoreCase(request.email())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Invalid activation code."));
        }

        if (user.getActivationCodeExpiresAt() == null) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(Map.of("message", "Activation code expired."));
        }

        if (user.getActivationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(Map.of("message", "Activation code expired."));
        }

        user.setActive(true);
        user.setActivationCode(null);
        user.setActivationCodeExpiresAt(null);

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "A fiok aktivalva."));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isValidPhoneNumber(String value) {
        return value != null && value.trim().matches("^\\+?\\d{8,15}$");
    }

    private boolean isValidPostalCode(String value) {
        return value != null && value.trim().matches("^\\d{4}$");
    }

    private String normalizeCompanyName(String value) {
        if (isBlank(value)) {
            return null;
        }

        return capitalizeWords(value);
    }

    private String capitalizeWords(String value) {
        if (value == null) {
            return "";
        }

        String[] words = value.trim().replaceAll("\\s+", " ").split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }

            if (!result.isEmpty()) {
                result.append(" ");
            }

            result.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
        }

        return result.toString();
    }

    private boolean isTitleCase(String value) {
        if (isBlank(value)) {
            return false;
        }

        for (String word : value.trim().split("\\s+")) {
            String firstLetter = word.substring(0, 1);
            if (!firstLetter.equals(firstLetter.toUpperCase())) {
                return false;
            }
        }

        return true;
    }

    private String createCode() {
        return String.valueOf(100000 + RANDOM.nextInt(900000));
    }

    public record CustomerRegistrationRequest(
            String firstName,
            String lastName,
            LocalDate birthDate,
            String email,
            String phoneNumber,
            String address,
            String city,
            String postalCode,
            String houseNumber,
            String companyName,
            String password,
            String passwordAgain
    ) {}

    public record ActivationRequest(
            String email,
            String token
    ) {}
}
