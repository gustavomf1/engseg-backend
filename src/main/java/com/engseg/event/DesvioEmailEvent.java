package com.engseg.event;

import com.engseg.entity.StatusDesvio;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.UUID;

@Getter
public class DesvioEmailEvent extends ApplicationEvent {

    private final UUID desvioId;
    private final StatusDesvio statusAnterior;
    private final StatusDesvio statusNovo;
    private final List<String> emailsManuais;
    private final List<String> emailsPadraoExcluidos;
    private final String comentario;
    private final UUID empresaContratadaId;

    public DesvioEmailEvent(Object source, UUID desvioId,
                            StatusDesvio statusAnterior,
                            StatusDesvio statusNovo,
                            List<String> emailsManuais,
                            List<String> emailsPadraoExcluidos,
                            String comentario,
                            UUID empresaContratadaId) {
        super(source);
        this.desvioId = desvioId;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.emailsManuais = emailsManuais != null ? emailsManuais : List.of();
        this.emailsPadraoExcluidos = emailsPadraoExcluidos != null ? emailsPadraoExcluidos : List.of();
        this.comentario = comentario;
        this.empresaContratadaId = empresaContratadaId;
    }
}
