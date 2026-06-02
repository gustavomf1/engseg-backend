package com.engseg.service;

import com.engseg.dto.request.*;
import com.engseg.dto.response.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncServiceTest {

    @Mock NaoConformidadeService ncService;
    @Mock DesvioService desvioService;
    @InjectMocks SyncService syncService;

    @Test
    void deveRetornarCRIADO_quandoNcProcessadaComSucesso() {
        NaoConformidadeRequest ncReq = new NaoConformidadeRequest(
                UUID.randomUUID(), "Titulo", null, "Desc", 3, 2,
                null, null, false, List.of(), false, null, List.of(), List.of(), null);
        SyncItemRequest item = new SyncItemRequest("local-1", "NC", ncReq, null);
        SyncBatchRequest batch = new SyncBatchRequest(List.of(item));

        NaoConformidadeResponse mockResponse = mock(NaoConformidadeResponse.class);
        when(mockResponse.id()).thenReturn(UUID.randomUUID());
        when(ncService.create(any())).thenReturn(mockResponse);

        SyncBatchResponse result = syncService.processar(batch);

        assertThat(result.results()).hasSize(1);
        assertThat(result.results().get(0).status()).isEqualTo("CRIADO");
        assertThat(result.results().get(0).localId()).isEqualTo("local-1");
        assertThat(result.results().get(0).serverId()).isNotNull();
    }

    @Test
    void deveRetornarERRO_quandoNcLancaExcecao() {
        NaoConformidadeRequest ncReq = new NaoConformidadeRequest(
                UUID.randomUUID(), "Titulo", null, "Desc", 3, 2,
                null, null, false, List.of(), false, null, List.of(), List.of(), null);
        SyncItemRequest item = new SyncItemRequest("local-2", "NC", ncReq, null);
        SyncBatchRequest batch = new SyncBatchRequest(List.of(item));

        when(ncService.create(any())).thenThrow(new RuntimeException("estabelecimento não encontrado"));

        SyncBatchResponse result = syncService.processar(batch);

        assertThat(result.results().get(0).status()).isEqualTo("ERRO");
        assertThat(result.results().get(0).erro()).contains("estabelecimento não encontrado");
    }
}
