package com.engseg.service;

import com.engseg.dto.request.RelatorioFiltroRequest;
import com.engseg.entity.*;
import com.engseg.repository.DesvioRepository;
import com.engseg.repository.NaoConformidadeRepository;
import com.engseg.util.ExcelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final NaoConformidadeRepository ncRepository;
    private final DesvioRepository desvioRepository;

    public byte[] gerarRelatorioNcs(RelatorioFiltroRequest filtro) throws IOException {
        LocalDateTime inicio = filtro.getDataInicio() != null ? filtro.getDataInicio().atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime fim = filtro.getDataFim() != null ? filtro.getDataFim().atTime(LocalTime.MAX) : LocalDateTime.now().plusYears(50);
        StatusNaoConformidade status = parseStatusNc(filtro.getStatus());

        List<NaoConformidade> ncs = ncRepository.findParaRelatorio(
            inicio, fim, filtro.getEstabelecimentoId(), filtro.getEmpresaContratadaId(), status);

        ExcelBuilder builder = new ExcelBuilder("NCs");
        builder.writeHeader(List.of(
            "DATA_REGISTRO", "TITULO", "ESTABELECIMENTO", "EMPRESA_CONTRATADA",
            "STATUS", "SEVERIDADE", "PROBABILIDADE", "NIVEL_RISCO",
            "REGRA_OURO", "RESPONSAVEL_NC", "RESPONSAVEL_TRATATIVA", "DATA_LIMITE", "VENCIDA"
        ));

        for (NaoConformidade nc : ncs) {
            builder.writeRow(List.of(
                nc.getDataRegistro().toString(),
                nc.getTitulo(),
                nc.getEstabelecimento().getNome(),
                nc.getEmpresaContratada() != null ? nc.getEmpresaContratada().getNomeFantasia() : "",
                nc.getStatus().toString(),
                nc.getSeveridade(),
                nc.getProbabilidade(),
                nc.getNivelRisco().toString(),
                nc.isRegraDeOuro() ? "SIM" : "NAO",
                nc.getResponsavelNc() != null ? nc.getResponsavelNc().getNome() : "",
                nc.getResponsavelTratativa() != null ? nc.getResponsavelTratativa().getNome() : "",
                nc.getDataLimiteResolucao() != null ? nc.getDataLimiteResolucao().toString() : "",
                nc.getVencida()
            ));
        }

        return builder.build();
    }

    public byte[] gerarRelatorioDesvios(RelatorioFiltroRequest filtro) throws IOException {
        LocalDateTime inicio = filtro.getDataInicio() != null ? filtro.getDataInicio().atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime fim = filtro.getDataFim() != null ? filtro.getDataFim().atTime(LocalTime.MAX) : LocalDateTime.now().plusYears(50);
        StatusDesvio status = filtro.getStatus() != null ? StatusDesvio.valueOf(filtro.getStatus()) : null;

        List<Desvio> desvios = desvioRepository.findParaRelatorio(
            inicio, fim, filtro.getEstabelecimentoId(), filtro.getEmpresaContratadaId(), status);

        ExcelBuilder builder = new ExcelBuilder("Desvios");
        builder.writeHeader(List.of(
            "DATA_REGISTRO", "TITULO", "ESTABELECIMENTO", "EMPRESA_CONTRATADA",
            "STATUS", "RESPONSAVEL_DESVIO", "RESPONSAVEL_TRATATIVA", "REGRA_OURO"
        ));

        for (Desvio d : desvios) {
            builder.writeRow(List.of(
                d.getDataRegistro().toString(),
                d.getTitulo(),
                d.getEstabelecimento().getNome(),
                d.getEmpresaContratada() != null ? d.getEmpresaContratada().getNomeFantasia() : "",
                d.getStatus().toString(),
                d.getResponsavelDesvio() != null ? d.getResponsavelDesvio().getNome() : "",
                d.getResponsavelTratativa() != null ? d.getResponsavelTratativa().getNome() : "",
                d.isRegraDeOuro() ? "SIM" : "NAO"
            ));
        }

        return builder.build();
    }

    public byte[] gerarRelatorioNcsVencidas(RelatorioFiltroRequest filtro) throws IOException {
        int dias = filtro.getDiasParaVencer() != null ? filtro.getDiasParaVencer() : 0;
        LocalDate dataLimite = LocalDate.now().plusDays(dias);

        List<NaoConformidade> ncs = ncRepository.findVencidasOuAVencer(
            dataLimite, filtro.getEstabelecimentoId(), filtro.getEmpresaContratadaId());

        ExcelBuilder builder = new ExcelBuilder("NCs_Vencidas");
        builder.writeHeader(List.of(
            "DATA_REGISTRO", "TITULO", "ESTABELECIMENTO", "EMPRESA_CONTRATADA",
            "STATUS", "SEVERIDADE", "PROBABILIDADE", "NIVEL_RISCO",
            "REGRA_OURO", "RESPONSAVEL_NC", "RESPONSAVEL_TRATATIVA", "DATA_LIMITE", "VENCIDA", "DIAS_ATRASO"
        ));

        LocalDate hoje = LocalDate.now();
        for (NaoConformidade nc : ncs) {
            long diasAtraso = nc.getDataLimiteResolucao() != null
                ? ChronoUnit.DAYS.between(nc.getDataLimiteResolucao(), hoje)
                : 0L;
            builder.writeRow(List.of(
                nc.getDataRegistro().toString(),
                nc.getTitulo(),
                nc.getEstabelecimento().getNome(),
                nc.getEmpresaContratada() != null ? nc.getEmpresaContratada().getNomeFantasia() : "",
                nc.getStatus().toString(),
                nc.getSeveridade(),
                nc.getProbabilidade(),
                nc.getNivelRisco().toString(),
                nc.isRegraDeOuro() ? "SIM" : "NAO",
                nc.getResponsavelNc() != null ? nc.getResponsavelNc().getNome() : "",
                nc.getResponsavelTratativa() != null ? nc.getResponsavelTratativa().getNome() : "",
                nc.getDataLimiteResolucao() != null ? nc.getDataLimiteResolucao().toString() : "",
                nc.getVencida(),
                diasAtraso
            ));
        }

        return builder.build();
    }

    public byte[] gerarResumoEmpresa(RelatorioFiltroRequest filtro) throws IOException {
        LocalDateTime inicio = filtro.getDataInicio() != null ? filtro.getDataInicio().atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime fim = filtro.getDataFim() != null ? filtro.getDataFim().atTime(LocalTime.MAX) : LocalDateTime.now().plusYears(50);

        List<NaoConformidade> ncs = ncRepository.findParaRelatorio(
            inicio, fim, filtro.getEstabelecimentoId(), filtro.getEmpresaContratadaId(), null);
        List<Desvio> desvios = desvioRepository.findParaRelatorio(
            inicio, fim, filtro.getEstabelecimentoId(), filtro.getEmpresaContratadaId(), null);

        // índices: 0=totalNcs, 1=abertas, 2=emTratamento, 3=concluidas, 4=naoResolvidas, 5=totalDesvios, 6=desviosConcluidos
        Map<String, long[]> resumo = new LinkedHashMap<>();

        for (NaoConformidade nc : ncs) {
            String empresa = nc.getEmpresaContratada() != null
                ? nc.getEmpresaContratada().getNomeFantasia() : "SEM_EMPRESA";
            long[] c = resumo.computeIfAbsent(empresa, k -> new long[7]);
            c[0]++;
            switch (nc.getStatus()) {
                case ABERTA -> c[1]++;
                case EM_TRATAMENTO -> c[2]++;
                case CONCLUIDO -> c[3]++;
                case NAO_RESOLVIDA -> c[4]++;
                default -> { }
            }
        }

        for (Desvio d : desvios) {
            String empresa = d.getEmpresaContratada() != null
                ? d.getEmpresaContratada().getNomeFantasia() : "SEM_EMPRESA";
            long[] c = resumo.computeIfAbsent(empresa, k -> new long[7]);
            c[5]++;
            if (d.getStatus() == StatusDesvio.CONCLUIDO) c[6]++;
        }

        ExcelBuilder builder = new ExcelBuilder("Resumo");
        builder.writeHeader(List.of(
            "EMPRESA_CONTRATADA", "TOTAL_NCS", "NCS_ABERTAS", "EM_TRATAMENTO",
            "CONCLUIDAS", "NAO_RESOLVIDAS", "TOTAL_DESVIOS", "DESVIOS_CONCLUIDOS"
        ));

        for (Map.Entry<String, long[]> entry : resumo.entrySet()) {
            long[] c = entry.getValue();
            builder.writeRow(List.of(entry.getKey(), c[0], c[1], c[2], c[3], c[4], c[5], c[6]));
        }

        return builder.build();
    }

    private StatusNaoConformidade parseStatusNc(String status) {
        return status != null ? StatusNaoConformidade.valueOf(status) : null;
    }
}
