package com.engseg.controller;

import com.engseg.config.SecurityConfig;
import com.engseg.security.JwtService;
import com.engseg.security.UserDetailsServiceImpl;
import com.engseg.service.RelatorioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RelatorioController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class RelatorioControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean RelatorioService relatorioService;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void relatorioNcs_retornaOkComContentTypeXlsx() throws Exception {
        when(relatorioService.gerarRelatorioNcs(any())).thenReturn(new byte[]{1, 2, 3});
        mockMvc.perform(get("/api/relatorios/ncs"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=relatorio-ncs.xlsx"))
            .andExpect(content().contentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void relatorioDesvios_retornaOkComContentTypeXlsx() throws Exception {
        when(relatorioService.gerarRelatorioDesvios(any())).thenReturn(new byte[]{1, 2, 3});
        mockMvc.perform(get("/api/relatorios/desvios"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=relatorio-desvios.xlsx"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void relatorioNcsVencidas_retornaOkComContentTypeXlsx() throws Exception {
        when(relatorioService.gerarRelatorioNcsVencidas(any())).thenReturn(new byte[]{1, 2, 3});
        mockMvc.perform(get("/api/relatorios/ncs-vencidas"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=relatorio-ncs-vencidas.xlsx"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void relatorioResumoEmpresa_retornaOkComContentTypeXlsx() throws Exception {
        when(relatorioService.gerarResumoEmpresa(any())).thenReturn(new byte[]{1, 2, 3});
        mockMvc.perform(get("/api/relatorios/resumo-empresa"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=relatorio-resumo-empresa.xlsx"));
    }

    @Test
    @WithMockUser(roles = "ENGENHEIRO")
    void relatorioNcs_comEngenheiroRole_retornaForbidden() throws Exception {
        mockMvc.perform(get("/api/relatorios/ncs"))
            .andExpect(status().isForbidden());
    }
}
