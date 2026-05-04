package com.engseg.event;

import com.engseg.entity.Desvio;
import com.engseg.entity.EmailPadrao;
import com.engseg.entity.StatusDesvio;
import com.engseg.repository.DesvioRepository;
import com.engseg.repository.EmailPadraoRepository;
import com.engseg.service.DesvioEmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class DesvioEmailListener {

    private final DesvioRepository desvioRepository;
    private final EmailPadraoRepository emailPadraoRepository;
    private final DesvioEmailSender desvioEmailSender;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onDesvioEmail(DesvioEmailEvent event) {
        log.info("DesvioEmailListener: evento recebido desvio={} {} -> {}", event.getDesvioId(), event.getStatusAnterior(), event.getStatusNovo());
        Desvio desvio = desvioRepository.findById(event.getDesvioId()).orElse(null);
        if (desvio == null) {
            log.warn("DesvioEmailListener: Desvio {} não encontrado, email ignorado", event.getDesvioId());
            return;
        }

        Set<String> dinamicos = new LinkedHashSet<>();
        if (desvio.getUsuarioCriacao() != null && desvio.getUsuarioCriacao().getEmail() != null)
            dinamicos.add(desvio.getUsuarioCriacao().getEmail());
        if (desvio.getResponsavelDesvio() != null && desvio.getResponsavelDesvio().getEmail() != null)
            dinamicos.add(desvio.getResponsavelDesvio().getEmail());
        if (desvio.getResponsavelTratativa() != null && desvio.getResponsavelTratativa().getEmail() != null)
            dinamicos.add(desvio.getResponsavelTratativa().getEmail());
        event.getEmailsManuais().stream().filter(Objects::nonNull).forEach(dinamicos::add);

        boolean isCriacaoOuConclusao = event.getStatusAnterior() == null
                || event.getStatusNovo() == StatusDesvio.CONCLUIDO;

        if (isCriacaoOuConclusao) {
            Set<String> padraoEfetivo = new LinkedHashSet<>();
            UUID empresaContratadaId = event.getEmpresaContratadaId();
            if (empresaContratadaId != null) {
                Set<String> excluidos = new HashSet<>(event.getEmailsPadraoExcluidos());
                emailPadraoRepository
                        .findByEstabelecimentoIdAndEmpresaId(desvio.getEstabelecimento().getId(), empresaContratadaId)
                        .stream()
                        .map(EmailPadrao::getEmail)
                        .filter(e -> !dinamicos.contains(e) && !excluidos.contains(e))
                        .forEach(padraoEfetivo::add);
            }
            Set<String> destinatarios = new LinkedHashSet<>(dinamicos);
            destinatarios.addAll(padraoEfetivo);
            if (!destinatarios.isEmpty()) {
                desvioEmailSender.enviarTemplateA(desvio, event.getStatusNovo(), destinatarios);
            }
        } else {
            if (!dinamicos.isEmpty()) {
                desvioEmailSender.enviarTemplateB(desvio, event.getStatusAnterior(),
                        event.getStatusNovo(), dinamicos, event.getComentario());
            }
        }
    }
}
