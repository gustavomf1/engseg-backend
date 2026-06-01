package com.engseg.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RedefinirSenhaRequest(
        @NotNull UUID resetToken,
        @NotBlank String novaSenha
) {}
