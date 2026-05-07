package com.backend.management.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private JavaMailSender levelKuldo;
    private String feladoCim;

    public EmailService(ObjectProvider<JavaMailSender> levelKuldoProvider,
                        @Value("${spring.mail.username:no-reply@management.local}") String feladoCim) {

        this.levelKuldo = levelKuldoProvider.getIfAvailable();
        this.feladoCim = feladoCim;
    }

    public boolean aktivaloKodKuldes(String cimzettCim, String kod) {

        String targy = "Management fiok aktivalasa";
        String szoveg = "Az aktivalo kodod: " + kod + "\n\nA kod 30 percig ervenyes.";

        return kodKuldes(cimzettCim, kod, targy, szoveg, "aktivalo");
    }

    public boolean jelszoVisszaallitoKodKuldes(String cimzettCim, String kod) {

        String targy = "Management jelszo visszaallitas";
        String szoveg = "A jelszo-visszaallito kodod: " + kod + "\n\nA kod 15 percig ervenyes.";

        return kodKuldes(cimzettCim, kod, targy, szoveg, "jelszo-visszaallito");
    }

    private boolean kodKuldes(String cimzettCim,
                             String kod,
                             String targy,
                             String szoveg,
                             String kodNev) {

        if (levelKuldo == null) {
            System.out.println("Nincs SMTP beallitva, nem lett elkuldve a(z) "
                    + kodNev + " kod: " + kod + " erre: " + cimzettCim);
            return false;
        }

        SimpleMailMessage uzenet = new SimpleMailMessage();

        uzenet.setFrom(feladoCim);
        uzenet.setTo(cimzettCim);
        uzenet.setSubject(targy);
        uzenet.setText(szoveg);

        try {
            levelKuldo.send(uzenet);
            return true;
        } catch (MailException e) {
            System.out.println("Hiba tortent email kuldes kozben: " + e.getMessage());
            return false;
        }
    }
}