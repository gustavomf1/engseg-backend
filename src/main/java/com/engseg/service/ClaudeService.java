package com.engseg.service;

import com.engseg.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
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
                "max_tokens", 4096,
                "system", "Você é um assistente especialista em normas reguladoras brasileiras. " +
                           "Dado o texto de uma NR, retorne TODOS os trechos relevantes para o prompt do usuário, " +
                           "incluindo cláusulas relacionadas ao mesmo tema mesmo que estejam em subseções diferentes. " +
                           "Retorne somente o texto das cláusulas, sem explicações adicionais. " +
                           "É melhor retornar mais do que menos — o usuário irá selecionar o que for pertinente.",
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
            log.info("[Claude] Enviando requisição → model={} max_tokens={} prompt_len={}",
                    MODEL, 4096, promptUsuario.length());

            ResponseEntity<String> response = restTemplate.exchange(
                    CLAUDE_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );

            log.info("[Claude] Resposta recebida → status={}", response.getStatusCode());
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("content").get(0).path("text").asText();
        } catch (HttpClientErrorException e) {
            log.error("[Claude] Erro HTTP {} → body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException("Erro ao consultar IA: HTTP " + e.getStatusCode() + " — " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[Claude] Erro inesperado → {}", e.getMessage(), e);
            throw new BusinessException("Erro ao consultar IA: " + e.getMessage());
        }
    }
}
