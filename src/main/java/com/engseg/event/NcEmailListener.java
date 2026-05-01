package com.engseg.event;

import com.engseg.entity.EmailPadrao;
import com.engseg.entity.NaoConformidade;
import com.engseg.entity.StatusNaoConformidade;
import com.engseg.entity.TipoEmailPadrao;
import com.engseg.repository.EmailPadraoRepository;
import com.engseg.repository.NaoConformidadeRepository;
import com.engseg.service.NcEmailSender;
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
public class NcEmailListener {

    private final NaoConformidadeRepository naoConformidadeRepository;
    private final EmailPadraoRepository emailPadraoRepository;
    private final NcEmailSender ncEmailSender;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onNcEmail(NcEmailEvent event) {
        NaoConformidade nc = naoConformidadeRepository.findById(event.getNcId()).orElse(null);
        if (nc == null) {
            log.warn("NcEmailListener: NC {} não encontrada, email ignorado", event.getNcId());
            return;
        }

        Set<String> dinamicos = new LinkedHashSet<>();
        if (nc.getUsuarioCriacao() != null && nc.getUsuarioCriacao().getEmail() != null)
            dinamicos.add(nc.getUsuarioCriacao().getEmail());
        if (nc.getEngResponsavelConstrutora() != null && nc.getEngResponsavelConstrutora().getEmail() != null)
            dinamicos.add(nc.getEngResponsavelConstrutora().getEmail());
        if (nc.getEngResponsavelVerificacao() != null && nc.getEngResponsavelVerificacao().getEmail() != null)
            dinamicos.add(nc.getEngResponsavelVerificacao().getEmail());
        event.getEmailsManuais().stream().filter(Objects::nonNull).forEach(dinamicos::add);

        StatusNaoConformidade statusNovo = event.getStatusNovo();
        boolean isAbertaOuConcluida = statusNovo == StatusNaoConformidade.ABERTA
                || statusNovo == StatusNaoConformidade.CONCLUIDO;

        if (isAbertaOuConcluida) {
            Set<String> padraoEfetivo = new LinkedHashSet<>();
            if (nc.getEngResponsavelConstrutora() != null) {
                UUID empresaId = nc.getEngResponsavelConstrutora().getEmpresa().getId();
                Set<String> excluidos = new HashSet<>(event.getEmailsPadraoExcluidos());
                emailPadraoRepository
                        .findByEstabelecimentoIdAndEmpresaIdAndTipo(nc.getEstabelecimento().getId(), empresaId, TipoEmailPadrao.NC)
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
}
