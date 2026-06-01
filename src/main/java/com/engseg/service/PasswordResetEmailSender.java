package com.engseg.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordResetEmailSender {

    private final BrevoEmailService brevoEmailService;

    public void enviar(String to, String otp) {
        // implementado na Task 5
    }
}
