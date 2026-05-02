package com.engseg.dto.request;

import java.util.List;

public record AprovarDesvioRequest(String comentario, List<String> emailsManuais) {}
