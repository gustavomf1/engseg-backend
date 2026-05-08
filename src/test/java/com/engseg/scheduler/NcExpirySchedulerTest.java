package com.engseg.scheduler;

import com.engseg.entity.NaoConformidade;
import com.engseg.event.kafka.ExpiryAlertEvent;
import com.engseg.repository.NaoConformidadeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NcExpirySchedulerTest {

    @Mock NaoConformidadeRepository ncRepository;
    @Mock KafkaTemplate<String, ExpiryAlertEvent> kafkaTemplate;
    @InjectMocks NcExpiryScheduler scheduler;

    @Test
    void devePublicarAlertaParaCadaNcVencendoEm10Dias() {
        NaoConformidade nc = new NaoConformidade();
        nc.setId(UUID.randomUUID());
        nc.setTitulo("NC Vencendo");
        nc.setDataLimiteResolucao(LocalDate.now().plusDays(10));

        when(ncRepository.findAtivasByDataLimiteResolucao(LocalDate.now().plusDays(10)))
                .thenReturn(List.of(nc));

        scheduler.verificarExpiry();

        ArgumentCaptor<ExpiryAlertEvent> captor = ArgumentCaptor.forClass(ExpiryAlertEvent.class);
        verify(kafkaTemplate).send(eq("engseg.expiry.alerts"), captor.capture());
        assertThat(captor.getValue().ncId()).isEqualTo(nc.getId());
        assertThat(captor.getValue().diasRestantes()).isEqualTo(10);
    }

    @Test
    void naoPublicaQuandoNaoHaNcsVencendo() {
        when(ncRepository.findAtivasByDataLimiteResolucao(any())).thenReturn(List.of());
        scheduler.verificarExpiry();
        verifyNoInteractions(kafkaTemplate);
    }
}
