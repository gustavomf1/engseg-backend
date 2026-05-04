package com.engseg.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class BrevoEmailService {

    private static final Logger log = LoggerFactory.getLogger(BrevoEmailService.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate;

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${app.mail.from}")
    private String fromEmail;

    public BrevoEmailService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void send(String to, String subject, String htmlContent) {
        Map<String, Object> body = Map.of(
                "sender", Map.of("name", "EngSeg", "email", fromEmail),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "htmlContent", htmlContent
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("accept", "application/json");

        ResponseEntity<String> response = restTemplate.postForEntity(
                BREVO_API_URL, new HttpEntity<>(body, headers), String.class);
        log.info("Brevo: email enviado para {} — status {}", to, response.getStatusCode());
    }
}
