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

import com.backend.management.enums.FeladatStatusz;
import com.backend.management.enums.Szerepkor;
import com.backend.management.model.Alkalmazott;
import com.backend.management.model.Csapat;
import com.backend.management.repository.AlkalmazottRepository;
import com.backend.management.repository.CsapatRepository;
import com.backend.management.repository.FeladatRepository;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = {"http://127.0.0.1:4200", "http://localhost:4200"})
public class TeamController {

    private final CsapatRepository csapatRepository;
    private final AlkalmazottRepository alkalmazottRepository;
    private final FeladatRepository feladatRepository;

    public TeamController(CsapatRepository csapatRepository,
                          AlkalmazottRepository alkalmazottRepository,
                          FeladatRepository feladatRepository) {
        this.csapatRepository = csapatRepository;
        this.alkalmazottRepository = alkalmazottRepository;
        this.feladatRepository = feladatRepository;
    }

    @GetMapping("/my")
    public ResponseEntity<?> sajatCsapatLekerdezes(@RequestParam String ownerEmail) {

        Alkalmazott tulajdonos = alkalmazottRepository.findByEmailIgnoreCase(ownerEmail).orElse(null);

        if (tulajdonos == null || !tulajdonos.isActive() || tulajdonos.getSzerepkor() != Szerepkor.PRODUCT_OWNER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv PRODUCT_OWNER kerheti le a csapatot."));
        }

        Csapat csapat = csapatRepository.findByTulajdonosEmailIgnoreCase(tulajdonos.getEmail()).orElse(null);

        if (csapat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Meg nincs csapatod."));
        }

        return ResponseEntity.ok(csapatValassaAlakitas(csapat));
    }

    @GetMapping
    public ResponseEntity<?> csapatokLekerdezes(@RequestParam String email) {

        Alkalmazott felhasznalo = alkalmazottRepository.findByEmailIgnoreCase(email).orElse(null);

        if (felhasznalo == null || !felhasznalo.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy DEVELOPER lathatja az osszes csapatot."));
        }

        if (felhasznalo.getSzerepkor() != Szerepkor.ADMIN &&
                felhasznalo.getSzerepkor() != Szerepkor.DEVELOPER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy DEVELOPER lathatja az osszes csapatot."));
        }

        List<TeamResponse> teams = csapatRepository.findAll()
                .stream()
                .map(this::csapatValassaAlakitas)
                .toList();

        return ResponseEntity.ok(teams);
    }

    @GetMapping("/membership")
    public ResponseEntity<?> tagsagLekerdezes(@RequestParam String email) {

        Alkalmazott felhasznalo = alkalmazottRepository.findByEmailIgnoreCase(email).orElse(null);

        if (felhasznalo == null || !felhasznalo.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy DEVELOPER kerheti le a sajat csapatat."));
        }

        if (felhasznalo.getSzerepkor() != Szerepkor.ADMIN &&
                felhasznalo.getSzerepkor() != Szerepkor.DEVELOPER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy DEVELOPER kerheti le a sajat csapatat."));
        }

        if (felhasznalo.getCsapat() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Meg nem vagy csapatban."));
        }

        return ResponseEntity.ok(csapatValassaAlakitas(felhasznalo.getCsapat()));
    }

    @PostMapping("/create")
    public ResponseEntity<?> csapatLetrehozas(@RequestBody CreateTeamRequest keres) {

        if (keres == null || keres.ownerEmail() == null || keres.ownerEmail().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "tulajdonos email kotelezo."));
        }

        Alkalmazott tulajdonos = alkalmazottRepository.findByEmailIgnoreCase(keres.ownerEmail()).orElse(null);

        if (tulajdonos == null || !tulajdonos.isActive() || tulajdonos.getSzerepkor() != Szerepkor.PRODUCT_OWNER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv PRODUCT_OWNER hozhat letre csapatot."));
        }

        if (csapatRepository.findByTulajdonosEmailIgnoreCase(tulajdonos.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Ennek a team ownernek mar van csapata."));
        }

        Csapat csapat = new Csapat(keres.name(), tulajdonos);
        Csapat mentett = csapatRepository.save(csapat);

        return ResponseEntity.ok(csapatValassaAlakitas(mentett));
    }

    @GetMapping("/available-developers")
    public ResponseEntity<?> szabadFejlesztokLekerdezes(@RequestParam String ownerEmail) {

        Alkalmazott tulajdonos = alkalmazottRepository.findByEmailIgnoreCase(ownerEmail).orElse(null);

        if (tulajdonos == null || !tulajdonos.isActive() || tulajdonos.getSzerepkor() != Szerepkor.PRODUCT_OWNER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv PRODUCT_OWNER lathatja a szabad DEVELOPERket."));
        }

        List<EmployeeResponse> developers = alkalmazottRepository
                .findBySzerepkorAndActiveTrueAndCsapatIsNull(Szerepkor.DEVELOPER)
                .stream()
                .map(this::alkalmazottValassaAlakitas)
                .toList();

        return ResponseEntity.ok(developers);
    }

    @PostMapping("/add-member")
    public ResponseEntity<?> tagHozzaadas(@RequestBody AddMemberRequest keres) {

        if (keres == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Hianyzo adatok."));
        }

        Alkalmazott tulajdonos = alkalmazottRepository.findByEmailIgnoreCase(keres.ownerEmail()).orElse(null);

        if (tulajdonos == null || !tulajdonos.isActive() || tulajdonos.getSzerepkor() != Szerepkor.PRODUCT_OWNER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv PRODUCT_OWNER vehet fel DEVELOPERt."));
        }

        Csapat csapat = csapatRepository.findByTulajdonosEmailIgnoreCase(tulajdonos.getEmail()).orElse(null);

        if (csapat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Eloszor hozz letre csapatot."));
        }

        Alkalmazott DEVELOPER = alkalmazottRepository.findById(keres.developerId()).orElse(null);

        if (DEVELOPER == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen DEVELOPER."));
        }

        if (!DEVELOPER.isActive() || DEVELOPER.getSzerepkor() != Szerepkor.DEVELOPER) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Csak aktiv DEVELOPER veheto fel csapatba."));
        }

        if (DEVELOPER.getCsapat() != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Ez a DEVELOPER mar csapatban van."));
        }

        DEVELOPER.setCsapat(csapat);
        alkalmazottRepository.save(DEVELOPER);

        Csapat frissitett = csapatRepository.findById(csapat.getId()).orElse(csapat);

        return ResponseEntity.ok(csapatValassaAlakitas(frissitett));
    }

    @PostMapping("/join")
    public ResponseEntity<?> csapathozCsatlakozas(@RequestBody JoinTeamRequest keres) {

        if (keres == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Hianyzo adatok."));
        }

        Alkalmazott felhasznalo = alkalmazottRepository.findByEmailIgnoreCase(keres.email()).orElse(null);

        if (felhasznalo == null || !felhasznalo.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy DEVELOPER csatlakozhat csapathoz."));
        }

        if (felhasznalo.getSzerepkor() != Szerepkor.ADMIN &&
                felhasznalo.getSzerepkor() != Szerepkor.DEVELOPER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy DEVELOPER csatlakozhat csapathoz."));
        }

        Csapat csapat = csapatRepository.findById(keres.teamId()).orElse(null);

        if (csapat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen csapat."));
        }

        if (felhasznalo.getCsapat() != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Mar tagja vagy egy csapatnak. Eloszor lepj ki onnan."));
        }

        felhasznalo.setCsapat(csapat);
        alkalmazottRepository.save(felhasznalo);

        Csapat frissitett = csapatRepository.findById(csapat.getId()).orElse(csapat);

        return ResponseEntity.ok(csapatValassaAlakitas(frissitett));
    }

    @PostMapping("/leave")
    public ResponseEntity<?> csapatElhagyas(@RequestBody LeaveTeamRequest keres) {

        if (keres == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Hianyzo adatok."));
        }

        Alkalmazott felhasznalo = alkalmazottRepository.findByEmailIgnoreCase(keres.email()).orElse(null);

        if (felhasznalo == null || !felhasznalo.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy DEVELOPER lephet ki csapatbol."));
        }

        if (felhasznalo.getSzerepkor() != Szerepkor.ADMIN &&
                felhasznalo.getSzerepkor() != Szerepkor.DEVELOPER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy DEVELOPER lephet ki csapatbol."));
        }

        if (felhasznalo.getCsapat() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Nem vagy csapatban."));
        }

        felhasznalo.setCsapat(null);

        nyitottFeladatokFelszabaditasa(felhasznalo);

        alkalmazottRepository.save(felhasznalo);

        return ResponseEntity.ok(Map.of("message", "Sikeresen kileptel a csapatbol."));
    }

    private void nyitottFeladatokFelszabaditasa(Alkalmazott DEVELOPER) {

        feladatRepository.findByMunkatarsIdAndStatuszNot(
                DEVELOPER.getId(),
                FeladatStatusz.DONE
        ).forEach(feladat -> {
            feladat.setMunkatars(null);
            feladat.setStatusz(FeladatStatusz.TO_DO);
            feladatRepository.save(feladat);
        });
    }

    private TeamResponse csapatValassaAlakitas(Csapat csapat) {

        List<EmployeeResponse> tagok = csapat.getTagok()
                .stream()
                .map(this::alkalmazottValassaAlakitas)
                .toList();

        return new TeamResponse(
                csapat.getId(),
                csapat.getNev(),
                alkalmazottValassaAlakitas(csapat.getTulajdonos()),
                tagok
        );
    }

    private EmployeeResponse alkalmazottValassaAlakitas(Alkalmazott alkalmazott) {
        return new EmployeeResponse(
                alkalmazott.getId(),
                alkalmazott.getKeresztnev(),
                alkalmazott.getVezeteknev(),
                alkalmazott.getEmail(),
                alkalmazott.getSzerepkor().name()
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

    public record EmployeeResponse(
            Long id,
            String keresztnev,
            String vezeteknev,
            String email,
            String szerepkor
    ) {}

    public record TeamResponse(
            Long id,
            String name,
            EmployeeResponse tulajdonos,
            List<EmployeeResponse> tagok
    ) {}
}