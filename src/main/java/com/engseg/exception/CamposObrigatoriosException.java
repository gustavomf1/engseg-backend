package com.engseg.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class CamposObrigatoriosException extends BusinessException {

    private final List<String> camposFaltantes;

    public CamposObrigatoriosException(String message, List<String> camposFaltantes) {
        super(message);
        this.camposFaltantes = camposFaltantes;
    }
}
