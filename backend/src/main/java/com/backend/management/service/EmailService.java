package com.backend.management.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private JavaMailSender mailSender;
    private String senderAddress;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                        @Value("${spring.mail.username:no-reply@management.local}") String senderAddress) {

        this.mailSender = mailSenderProvider.getIfAvailable();
        this.senderAddress = senderAddress;
    }

    public boolean sendActivationCode(String recipientAddress, String code) {
        return sendActivationCode(recipientAddress, code, "en");
    }

    public boolean sendActivationCode(String recipientAddress, String code, String language) {

        boolean english = "en".equalsIgnoreCase(language);

        String subject = english
                ? "Account activation"
                : "Fiok aktivalasa";
        String text = english
                ? "Your activation code: " + code + "\n\nThe code is valid for 30 minutes."
                : "Az aktivalo kodod: " + code + "\n\nA kod 30 percig ervenyes.";

        return sendCode(recipientAddress, code, subject, text, "activation");
    }

    public boolean sendPasswordResetCode(String recipientAddress, String code) {

        String subject = "Management password reset";
        String text = "Your password reset code: " + code + "\n\nThe code is valid for 15 minutes.";

        return sendCode(recipientAddress, code, subject, text, "password reset");
    }

    private boolean sendCode(String recipientAddress,
                             String code,
                             String subject,
                             String text,
                             String codeName) {

        if (mailSender == null || senderAddress == null || senderAddress.isBlank()) {
            System.out.println("SMTP is not configured, the "
                    + codeName + " code was not sent to: " + recipientAddress);
            return false;
        }

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderAddress);
        message.setTo(recipientAddress);
        message.setSubject(subject);
        message.setText(text);

        try {
            mailSender.send(message);
            return true;
        } catch (MailException e) {
            System.out.println("Email sending failed: " + e.getMessage());
            return false;
        }
    }
}
