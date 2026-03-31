package com.engseg.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record EstabelecimentoResponse(
        UUID id,
        String nome,
        String codigo,
        UUID empresaId,
        String empresaNome,
        String cep,
        String logradouro,
        String numero,
        String bairro,
        String cidade,
        String estado,
        boolean ativo,
        LocalDate dtInativacao
) {}
