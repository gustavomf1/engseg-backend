package com.engseg.scheduler;

import com.engseg.service.NaoConformidadeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NcSchedulerTest {

    @Mock NaoConformidadeService naoConformidadeService;

    @InjectMocks
    NcScheduler scheduler;

    @Test
    void atualizarVencidas_delegaParaService() {
        scheduler.atualizarVencidas();

        verify(naoConformidadeService).atualizarVencidas();
    }
}
