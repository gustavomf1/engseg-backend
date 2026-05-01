package com.engseg.dto.request;

import java.util.List;

public record AprovarRejeitarRequest(
        String comentario,
        List<String> emailsManuais
) {}
