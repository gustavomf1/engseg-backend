package com.engseg.service;

import com.engseg.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClaudeService {

    private static final String CLAUDE_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-haiku-4-5-20251001";

    @Value("${claude.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String buscarTrecho(String conteudoNorma, String promptUsuario) {
        Map<String, Object> body = Map.of(
                "model", MODEL,
                "max_tokens", 1024,
                "system", "Você é um assistente especialista em normas reguladoras brasileiras. " +
                           "Dado o texto de uma NR, retorne APENAS o trecho mais relevante para o prompt do usuário. " +
                           "Retorne somente o texto da cláusula, sem explicações adicionais.",
                "messages", List.of(
                        Map.of("role", "user", "content",
                                "Prompt: " + promptUsuario + "\n\nTexto da NR:\n" + conteudoNorma)
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    CLAUDE_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("content").get(0).path("text").asText();
        } catch (Exception e) {
            throw new BusinessException("Erro ao consultar IA: " + e.getMessage());
        }
    }
}
