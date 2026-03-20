package com.engseg.dto.response;

public record DashboardStatsResponse(
        long totalOcorrencias,
        long totalDesvios,
        long totalNaoConformidades,
        long totalRegraDeOuro,
        long ncAbertas,
        long ncEmTratamento,
        long ncConcluidas,
        long ncNaoResolvidas,
        long totalDesviosConcluidos
) {}
