package com.engseg.event;

import com.engseg.entity.*;
import com.engseg.event.kafka.NcKafkaEvent;
import com.engseg.repository.EmailPadraoRepository;
import com.engseg.repository.NaoConformidadeRepository;
import com.engseg.service.NcEmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NcEmailListener {

    private static final String TOPIC = "engseg.nc.events";

    private final NaoConformidadeRepository naoConformidadeRepository;
    private final EmailPadraoRepository emailPadraoRepository;
    private final NcEmailSender ncEmailSender;
    private final KafkaTemplate<String, NcKafkaEvent> kafkaTemplate;
    private final NcPushMessageBuilder pushMessageBuilder;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onNcEmail(NcEmailEvent event) {
        NaoConformidade nc = naoConformidadeRepository.findById(event.getNcId()).orElse(null);
        if (nc == null) {
            log.warn("NcEmailListener: NC {} não encontrada, ignorado", event.getNcId());
            return;
        }

        enviarEmail(nc, event);
        publicarKafka(nc, event);
    }

    private void enviarEmail(NaoConformidade nc, NcEmailEvent event) {
        boolean isCriacao = event.getStatusAnterior() == null;

        Set<String> dinamicos = new LinkedHashSet<>();
        if (nc.getUsuarioCriacao() != null && nc.getUsuarioCriacao().getEmail() != null)
            dinamicos.add(nc.getUsuarioCriacao().getEmail());
        if (nc.getResponsavelNc() != null && nc.getResponsavelNc().getEmail() != null)
            dinamicos.add(nc.getResponsavelNc().getEmail());
        // responsável pela tratativa só recebe a partir de AGUARDANDO_APROVACAO_PLANO, não na abertura
        if (!isCriacao && nc.getResponsavelTratativa() != null && nc.getResponsavelTratativa().getEmail() != null)
            dinamicos.add(nc.getResponsavelTratativa().getEmail());
        if (nc.getEmailsManuais() != null)
            nc.getEmailsManuais().stream().filter(Objects::nonNull).forEach(dinamicos::add);
        event.getEmailsManuais().stream().filter(Objects::nonNull).forEach(dinamicos::add);

        StatusNaoConformidade statusNovo = event.getStatusNovo();
        boolean isAbertaOuConcluida = isCriacao || statusNovo == StatusNaoConformidade.CONCLUIDO;

        if (isAbertaOuConcluida) {
            Set<String> padraoEfetivo = new LinkedHashSet<>();
            if (nc.getResponsavelNc() != null) {
                UUID empresaId = nc.getResponsavelNc().getEmpresa().getId();
                Set<String> excluidos = new HashSet<>(event.getEmailsPadraoExcluidos());
                // na criação, responsavelTratativa não está em dinamicos mas também não deve receber via padrão
                if (isCriacao && nc.getResponsavelTratativa() != null && nc.getResponsavelTratativa().getEmail() != null)
                    excluidos.add(nc.getResponsavelTratativa().getEmail());
                emailPadraoRepository
                        .findByEstabelecimentoIdAndEmpresaId(nc.getEstabelecimento().getId(), empresaId)
                        .stream()
                        .map(EmailPadrao::getEmail)
                        .filter(e -> !dinamicos.contains(e) && !excluidos.contains(e))
                        .forEach(padraoEfetivo::add);
            }
            Set<String> destinatarios = new LinkedHashSet<>(dinamicos);
            destinatarios.addAll(padraoEfetivo);
            if (!destinatarios.isEmpty()) {
                ncEmailSender.enviarTemplateA(nc, statusNovo, destinatarios);
            }
        } else {
            if (!dinamicos.isEmpty()) {
                ncEmailSender.enviarTemplateB(nc, event.getStatusAnterior(), statusNovo,
                        dinamicos, event.getComentario());
            }
        }
    }

    private void publicarKafka(NaoConformidade nc, NcEmailEvent event) {
        NcKafkaEvent kafkaEvent = pushMessageBuilder.resolver(
                nc, event.getStatusAnterior(), event.getStatusNovo(), event.getComentario());
        if (kafkaEvent == null) {
            log.debug("NcEmailListener: par de status sem mapeamento de push, Kafka não publicado para NC {}", nc.getId());
            return;
        }
        kafkaTemplate.send(TOPIC, kafkaEvent);
        log.info("NcEmailListener: Kafka {} publicado para NC {}", kafkaEvent.tipo(), nc.getId());
    }
}
