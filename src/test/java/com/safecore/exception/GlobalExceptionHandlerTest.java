package com.safecore.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleCamposObrigatorios_retorna422ComListaDeCampos() {
        var ex = new CamposObrigatoriosException("Preencha os campos obrigatórios.", List.of("DESCRICAO", "NORMA_VINCULADA"));

        ResponseEntity<GlobalExceptionHandler.CamposObrigatoriosErrorResponse> response = handler.handleCamposObrigatorios(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().camposFaltantes()).containsExactly("DESCRICAO", "NORMA_VINCULADA");
        assertThat(response.getBody().message()).isEqualTo("Preencha os campos obrigatórios.");
    }
}
