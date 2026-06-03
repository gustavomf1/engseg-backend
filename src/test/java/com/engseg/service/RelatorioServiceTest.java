package com.engseg.service;

import com.engseg.dto.request.RelatorioFiltroRequest;
import com.engseg.entity.*;
import com.engseg.repository.DesvioRepository;
import com.engseg.repository.NaoConformidadeRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock NaoConformidadeRepository ncRepository;
    @Mock DesvioRepository desvioRepository;
    @InjectMocks RelatorioService service;

    private Estabelecimento buildEstabelecimento() {
        Estabelecimento est = new Estabelecimento();
        est.setId(UUID.randomUUID());
        est.setNome("Est. Teste");
        return est;
    }

    private NaoConformidade buildNc() {
        NaoConformidade nc = new NaoConformidade();
        nc.setId(UUID.randomUUID());
        nc.setTitulo("NC Teste");
        nc.setDescricao("Descricao");
        nc.setDataRegistro(LocalDateTime.now());
        nc.setEstabelecimento(buildEstabelecimento());
        nc.setStatus(StatusNaoConformidade.ABERTA);
        nc.setSeveridade(3);
        nc.setProbabilidade(2);
        nc.setNivelRisco(NivelRisco.ALTO);
        nc.setRegraDeOuro(false);
        nc.setVencida("N");
        return nc;
    }

    private Desvio buildDesvio() {
        Desvio d = new Desvio();
        d.setId(UUID.randomUUID());
        d.setTitulo("Desvio Teste");
        d.setDescricao("Descricao");
        d.setDataRegistro(LocalDateTime.now());
        d.setEstabelecimento(buildEstabelecimento());
        d.setStatus(StatusDesvio.AGUARDANDO_TRATATIVA);
        d.setRegraDeOuro(false);
        d.setOrientacaoRealizada("Orientação");
        return d;
    }

    @Test
    void gerarRelatorioNcs_semDados_retornaExcelComCabecalho() throws IOException {
        when(ncRepository.findParaRelatorio(any(), any(), any(), any(), any())).thenReturn(List.of());
        byte[] bytes = service.gerarRelatorioNcs(new RelatorioFiltroRequest());
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = wb.getSheetAt(0);
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("DATA_REGISTRO");
            assertThat(sheet.getLastRowNum()).isEqualTo(0);
        }
    }

    @Test
    void gerarRelatorioNcs_comUmaNC_retornaUmaLinhaDeDados() throws IOException {
        when(ncRepository.findParaRelatorio(any(), any(), any(), any(), any())).thenReturn(List.of(buildNc()));
        byte[] bytes = service.gerarRelatorioNcs(new RelatorioFiltroRequest());
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertThat(wb.getSheetAt(0).getLastRowNum()).isEqualTo(1);
        }
    }

    @Test
    void gerarRelatorioDesvios_comUmDesvio_retornaUmaLinhaDeDados() throws IOException {
        when(desvioRepository.findParaRelatorio(any(), any(), any(), any(), any())).thenReturn(List.of(buildDesvio()));
        byte[] bytes = service.gerarRelatorioDesvios(new RelatorioFiltroRequest());
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertThat(wb.getSheetAt(0).getLastRowNum()).isEqualTo(1);
        }
    }

    @Test
    void gerarRelatorioNcsVencidas_semDados_retornaExcelComCabecalhoDiasAtraso() throws IOException {
        when(ncRepository.findVencidasOuAVencer(any(), any(), any())).thenReturn(List.of());
        byte[] bytes = service.gerarRelatorioNcsVencidas(new RelatorioFiltroRequest());
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Row header = wb.getSheetAt(0).getRow(0);
            assertThat(header.getCell(13).getStringCellValue()).isEqualTo("DIAS_ATRASO");
        }
    }

    @Test
    void gerarResumoEmpresa_semDados_retornaExcelComCabecalhoEmpresaContratada() throws IOException {
        when(ncRepository.findParaRelatorio(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(desvioRepository.findParaRelatorio(any(), any(), any(), any(), any())).thenReturn(List.of());
        byte[] bytes = service.gerarResumoEmpresa(new RelatorioFiltroRequest());
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertThat(wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue()).isEqualTo("EMPRESA_CONTRATADA");
        }
    }

    @Test
    void gerarResumoEmpresa_comNcEDesvioMesmaEmpresa_retornaUmaLinhaDeResumo() throws IOException {
        Empresa emp = new Empresa();
        emp.setId(UUID.randomUUID());
        emp.setNomeFantasia("Empresa Alpha");

        NaoConformidade nc = buildNc();
        nc.setEmpresaContratada(emp);

        Desvio d = buildDesvio();
        d.setEmpresaContratada(emp);

        when(ncRepository.findParaRelatorio(any(), any(), any(), any(), any())).thenReturn(List.of(nc));
        when(desvioRepository.findParaRelatorio(any(), any(), any(), any(), any())).thenReturn(List.of(d));

        byte[] bytes = service.gerarResumoEmpresa(new RelatorioFiltroRequest());
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = wb.getSheetAt(0);
            assertThat(sheet.getLastRowNum()).isEqualTo(1);
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("Empresa Alpha");
        }
    }
}
