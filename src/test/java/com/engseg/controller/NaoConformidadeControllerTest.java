package com.engseg.controller;

import com.engseg.config.SecurityConfig;
import com.engseg.dto.request.RejeitarRequest;
import com.engseg.dto.response.NaoConformidadeResponse;
import com.engseg.security.JwtFilter;
import com.engseg.security.JwtService;
import com.engseg.security.UserDetailsServiceImpl;
import com.engseg.service.NaoConformidadeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de segurança dos endpoints de Não Conformidade.
 * Verificam se os @PreAuthorize bloqueiam os perfis não autorizados.
 */
@WebMvcTest(NaoConformidadeController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class NaoConformidadeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean NaoConformidadeService naoConformidadeService;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    private final UUID ncId = UUID.randomUUID();

    // ─── Sem autenticação ──────────────────────────────────────────────────────

    @Test
    void getAll_semAutenticacao_retorna4xx() throws Exception {
        // API stateless JWT retorna 403 para requests sem autenticação (sem AuthenticationEntryPoint customizado)
        mockMvc.perform(get("/api/nao-conformidades"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void aprovarPlano_semAutenticacao_retorna4xx() throws Exception {
        mockMvc.perform(post("/api/nao-conformidades/{id}/aprovar-plano", ncId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is4xxClientError());
    }

    // ─── EXTERNO: acesso permitido ─────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "EXTERNO")
    void getAll_externoAutenticado_retorna200() throws Exception {
        when(naoConformidadeService.findAll(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/nao-conformidades"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EXTERNO")
    void getById_externoAutenticado_retorna200() throws Exception {
        when(naoConformidadeService.findById(any())).thenReturn(mockNcResponse());

        mockMvc.perform(get("/api/nao-conformidades/{id}", ncId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EXTERNO")
    void submeterInvestigacao_externoAutenticado_retorna200() throws Exception {
        when(naoConformidadeService.submeterInvestigacao(any(), any())).thenReturn(mockNcResponse());
        String body = objectMapper.writeValueAsString(new com.engseg.dto.request.InvestigacaoRequest(
                List.of(new com.engseg.dto.request.InvestigacaoRequest.PorqueItem("P1", "R1")),
                "Causa raiz", List.of(new com.engseg.dto.request.InvestigacaoRequest.AtividadeItem("Título 1", "Atividade 1")),
                null
        ));

        mockMvc.perform(post("/api/nao-conformidades/{id}/investigacao", ncId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    // ─── EXTERNO: acesso bloqueado (apenas ENGENHEIRO) ─────────────────────────

    @Test
    @WithMockUser(roles = "EXTERNO")
    void aprovarPlano_externoAutenticado_retorna403() throws Exception {
        mockMvc.perform(post("/api/nao-conformidades/{id}/aprovar-plano", ncId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EXTERNO")
    void rejeitarPlano_externoAutenticado_retorna403() throws Exception {
        String body = objectMapper.writeValueAsString(new RejeitarRequest("motivo", null));

        mockMvc.perform(post("/api/nao-conformidades/{id}/rejeitar-plano", ncId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EXTERNO")
    void aprovarEvidencias_externoAutenticado_retorna403() throws Exception {
        mockMvc.perform(post("/api/nao-conformidades/{id}/aprovar-evidencias", ncId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EXTERNO")
    void rejeitarEvidencias_externoAutenticado_retorna403() throws Exception {
        String body = objectMapper.writeValueAsString(new RejeitarRequest("motivo", null));

        mockMvc.perform(post("/api/nao-conformidades/{id}/rejeitar-evidencias", ncId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EXTERNO")
    void criarNc_externoAutenticado_retorna403() throws Exception {
        // Corpo válido para passar @Valid — a autorização é verificada depois
        String body = String.format(
                "{\"estabelecimentoId\":\"%s\",\"titulo\":\"Teste\",\"descricao\":\"Desc\",\"severidade\":2,\"probabilidade\":2,\"regraDeOuro\":false,\"reincidencia\":false}",
                UUID.randomUUID()
        );
        mockMvc.perform(post("/api/nao-conformidades")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    // ─── TECNICO: acesso permitido ─────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "TECNICO")
    void getAll_tecnicoAutenticado_retorna200() throws Exception {
        when(naoConformidadeService.findAll(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/nao-conformidades"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TECNICO")
    void aprovarPlano_tecnicoAutenticado_retorna403() throws Exception {
        mockMvc.perform(post("/api/nao-conformidades/{id}/aprovar-plano", ncId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // ─── ENGENHEIRO: acesso completo ───────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ENGENHEIRO")
    void aprovarPlano_engenheiroAutenticado_retorna200() throws Exception {
        when(naoConformidadeService.aprovarPlano(any(), any())).thenReturn(mockNcResponse());

        mockMvc.perform(post("/api/nao-conformidades/{id}/aprovar-plano", ncId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ENGENHEIRO")
    void rejeitarPlano_engenheiroAutenticado_retorna200() throws Exception {
        when(naoConformidadeService.rejeitarPlano(any(), any())).thenReturn(mockNcResponse());
        String body = objectMapper.writeValueAsString(new RejeitarRequest("motivo válido", null));

        mockMvc.perform(post("/api/nao-conformidades/{id}/rejeitar-plano", ncId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ENGENHEIRO")
    void aprovarEvidencias_engenheiroAutenticado_retorna200() throws Exception {
        when(naoConformidadeService.aprovarEvidencias(any(), any())).thenReturn(mockNcResponse());

        mockMvc.perform(post("/api/nao-conformidades/{id}/aprovar-evidencias", ncId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ENGENHEIRO")
    void rejeitarEvidencias_engenheiroAutenticado_retorna200() throws Exception {
        when(naoConformidadeService.rejeitarEvidencias(any(), any())).thenReturn(mockNcResponse());
        String body = objectMapper.writeValueAsString(new RejeitarRequest("motivo válido", null));

        mockMvc.perform(post("/api/nao-conformidades/{id}/rejeitar-evidencias", ncId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    // ─── Validação HTTP (@Valid) ───────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ENGENHEIRO")
    void rejeitarPlano_motivoVazio_retorna400() throws Exception {
        String body = objectMapper.writeValueAsString(new RejeitarRequest("", null));

        mockMvc.perform(post("/api/nao-conformidades/{id}/rejeitar-plano", ncId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ENGENHEIRO")
    void rejeitarEvidencias_motivoVazio_retorna400() throws Exception {
        String body = objectMapper.writeValueAsString(new RejeitarRequest("", null));

        mockMvc.perform(post("/api/nao-conformidades/{id}/rejeitar-evidencias", ncId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ─── helper ───────────────────────────────────────────────────────────────

    private NaoConformidadeResponse mockNcResponse() {
        return new NaoConformidadeResponse(
                ncId,                                                    // id
                UUID.randomUUID(),                                       // estabelecimentoId
                "Estabelecimento",                                       // estabelecimentoNome
                "NC Teste",                                              // titulo
                null, null,                                              // localizacaoId, localizacaoNome
                "Descrição",                                             // descricao
                null,                                                    // dataRegistro
                null,                                                    // tecnicoNome
                false,                                                   // regraDeOuro
                2,                                                       // severidade
                2,                                                       // probabilidade
                com.engseg.entity.NivelRisco.BAIXO,                     // nivelRisco
                null, null, null, null,                                  // engConstrutora id/nome/email/perfil
                null, null, null, null,                                  // engVerificacao id/nome/email/perfil
                java.time.LocalDate.now().plusDays(30),                  // dataLimiteResolucao
                null, null,                                              // usuarioCriacao nome/email
                com.engseg.entity.StatusNaoConformidade.ABERTA,         // status
                false, false,                                            // vencida, reincidencia
                null, null,                                              // ncAnteriorId, ncAnteriorTitulo
                List.of(), List.of(),                                    // cadeiaReincidencias, reincidencias
                null, null, null, null, null, null,                      // porques 1-3 + respostas
                null, null, null, null,                                  // porques 4-5 + respostas
                null,                                                    // causaRaiz
                null,                                                    // descricaoExecucao
                List.of(),                                               // atividades
                List.of(),                                               // historico
                List.of(),                                               // investigacaoSnapshots
                List.of(),                                               // execucaoSnapshots
                List.of(),                                               // devolutivas
                List.of(),                                               // execucoes
                List.of(),                                               // validacoes
                List.of()                                                // normas
        );
    }
}
