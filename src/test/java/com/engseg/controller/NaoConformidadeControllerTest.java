package com.engseg.controller;

import com.engseg.config.SecurityConfig;
import com.engseg.dto.request.RejeitarRequest;
import com.engseg.dto.response.NaoConformidadeResponse;
import com.engseg.exception.CamposObrigatoriosException;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    void getAll_semAutenticacao_retorna4xx() throws Exception {

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

    @Test
    @WithMockUser(roles = "EXTERNO")
    void getAll_externoAutenticado_retorna200() throws Exception {
        when(naoConformidadeService.findAll(any(), any(), any(), any())).thenReturn(List.of());

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

        String body = String.format(
                "{\"estabelecimentoId\":\"%s\",\"titulo\":\"Teste\",\"localizacaoId\":\"%s\",\"descricao\":\"Desc\",\"severidade\":2,\"probabilidade\":2,\"regraDeOuro\":false,\"reincidencia\":false,\"empresaContratadaId\":\"%s\"}",
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
        );
        mockMvc.perform(post("/api/nao-conformidades")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TECNICO")
    void getAll_tecnicoAutenticado_retorna200() throws Exception {
        when(naoConformidadeService.findAll(any(), any(), any(), any())).thenReturn(List.of());

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

    @Test
    @WithMockUser(roles = "ENGENHEIRO")
    void ativar_quandoFaltamCampos_retorna422ComCamposFaltantes() throws Exception {
        when(naoConformidadeService.ativar(any()))
                .thenThrow(new CamposObrigatoriosException(
                        "Preencha os campos obrigatórios antes de enviar para o Plano de Ação.",
                        List.of("DESCRICAO", "NORMA_VINCULADA")));

        mockMvc.perform(post("/api/nao-conformidades/{id}/ativar", ncId).with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.camposFaltantes[0]").value("DESCRICAO"))
                .andExpect(jsonPath("$.camposFaltantes[1]").value("NORMA_VINCULADA"));
    }

    private NaoConformidadeResponse mockNcResponse() {
        return new NaoConformidadeResponse(
                ncId,
                "NC-0001",
                UUID.randomUUID(),
                "Estabelecimento",
                "NC Teste",
                null, null,
                "Descrição",
                null,
                null,
                false,
                2,
                2,
                com.engseg.entity.NivelRisco.BAIXO,
                null, null, null, null,
                null, null, null, null,
                java.time.LocalDate.now().plusDays(30),
                null, null,
                com.engseg.entity.StatusNaoConformidade.ABERTA,
                false, false,
                null, null,
                List.of(), List.of(),
                null, null, null, null, null, null,
                null, null, null, null,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                null, null
        );
    }
}
