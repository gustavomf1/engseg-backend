package com.engseg.event;

import com.engseg.entity.StatusNaoConformidade;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.UUID;

@Getter
public class NcEmailEvent extends ApplicationEvent {

    private final UUID ncId;
    private final StatusNaoConformidade statusAnterior;
    private final StatusNaoConformidade statusNovo;
    private final List<String> emailsManuais;
    private final List<String> emailsPadraoExcluidos;
    private final String comentario;

    public NcEmailEvent(Object source, UUID ncId,
                        StatusNaoConformidade statusAnterior,
                        StatusNaoConformidade statusNovo,
                        List<String> emailsManuais,
                        List<String> emailsPadraoExcluidos,
                        String comentario) {
        super(source);
        this.ncId = ncId;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.emailsManuais = emailsManuais != null ? emailsManuais : List.of();
        this.emailsPadraoExcluidos = emailsPadraoExcluidos != null ? emailsPadraoExcluidos : List.of();
        this.comentario = comentario;
    }
}
