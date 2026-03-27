package com.engseg.entity;

public enum StatusNaoConformidade {
    // Fluxo atual
    ABERTA,
    AGUARDANDO_APROVACAO_PLANO,
    EM_AJUSTE_PELO_EXTERNO,
    EM_EXECUCAO,
    AGUARDANDO_VALIDACAO_FINAL,
    CONCLUIDO,
    // Legado (mantidos para compatibilidade com dados existentes)
    EM_TRATAMENTO,
    NAO_RESOLVIDA
}
