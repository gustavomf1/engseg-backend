package com.engseg.scheduler;

import com.engseg.entity.NaoConformidade;
import com.engseg.event.kafka.ExpiryAlertEvent;
import com.engseg.repository.NaoConformidadeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NcExpiryScheduler {

    private static final String TOPIC = "engseg.expiry.alerts";
    private static final int DIAS_ANTECEDENCIA = 10;

    private final NaoConformidadeRepository ncRepository;
    private final KafkaTemplate<String, ExpiryAlertEvent> kafkaTemplate;

    @Scheduled(cron = "0 0 0 * * *")
    public void verificarExpiry() {
        LocalDate dataAlvo = LocalDate.now().plusDays(DIAS_ANTECEDENCIA);
        List<NaoConformidade> ncs = ncRepository.findAtivasByDataLimiteResolucao(dataAlvo);
        log.info("NcExpiryScheduler: {} NCs vencendo em {} dias", ncs.size(), DIAS_ANTECEDENCIA);

        for (NaoConformidade nc : ncs) {
            UUID responsavelId = nc.getEngResponsavelVerificacao() != null
                    ? nc.getEngResponsavelVerificacao().getId() : null;
            ExpiryAlertEvent event = new ExpiryAlertEvent(
                    nc.getId(), nc.getTitulo(), DIAS_ANTECEDENCIA, responsavelId);
            kafkaTemplate.send(TOPIC, event);
        }
    }
}
