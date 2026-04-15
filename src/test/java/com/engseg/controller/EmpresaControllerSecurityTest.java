package com.engseg.controller;

import com.engseg.config.SecurityConfig;
import com.engseg.security.JwtService;
import com.engseg.security.UserDetailsServiceImpl;
import com.engseg.service.EmpresaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmpresaController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class EmpresaControllerSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    EmpresaService empresaService;

    @MockBean
    JwtService jwtService;

    @MockBean
    UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "ENGENHEIRO")
    void createEmpresa_engenheiroRole_returns403() throws Exception {
        // body válido para passar @Valid — a barreira de autorização (@PreAuthorize) é verificada depois
        mockMvc.perform(post("/api/empresas")
                .contentType("application/json")
                .content("{\"razaoSocial\":\"Empresa Teste\",\"cnpj\":\"00.000.000/0001-00\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEmpresa_adminRole_doesNotReturn403() throws Exception {
        // body válido; ADMIN passa @PreAuthorize → serviço retorna null (mock) → 500, mas não 403
        mockMvc.perform(post("/api/empresas")
                .contentType("application/json")
                .content("{\"razaoSocial\":\"Empresa Teste\",\"cnpj\":\"00.000.000/0001-00\"}"))
                .andExpect(result ->
                    assertThat(result.getResponse().getStatus()).isNotEqualTo(403));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listEmpresas_adminRole_returns200() throws Exception {
        when(empresaService.findAll(any())).thenReturn(List.of());
        mockMvc.perform(get("/api/empresas"))
                .andExpect(status().isOk());
    }
}
