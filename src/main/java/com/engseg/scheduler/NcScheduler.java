package com.engseg.scheduler;

import com.engseg.service.NaoConformidadeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NcScheduler {

    private final NaoConformidadeService naoConformidadeService;

    @Scheduled(cron = "0 0 0 * * *")
    public void atualizarVencidas() {
        naoConformidadeService.atualizarVencidas();
    }
}
