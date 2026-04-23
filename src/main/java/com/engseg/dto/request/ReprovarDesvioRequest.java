package com.engseg.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReprovarDesvioRequest(@NotBlank String motivo) {}
