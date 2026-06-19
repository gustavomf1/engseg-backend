package com.engseg.event;

import com.engseg.entity.*;
import com.engseg.event.kafka.NcKafkaEvent;
import com.engseg.repository.EmailPadraoRepository;
import com.engseg.repository.NaoConformidadeRepository;
import com.engseg.service.NcEmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class NcEmailListenerTest {

    @Mock NaoConformidadeRepository ncRepository;
    @Mock EmailPadraoRepository emailPadraoRepository;
    @Mock NcEmailSender sender;
    @Mock KafkaTemplate<String, NcKafkaEvent> kafkaTemplate;
    @Mock NcPushMessageBuilder pushMessageBuilder;
    @InjectMocks NcEmailListener listener;

    private NaoConformidade nc;
    private Estabelecimento estabelecimento;
    private Usuario responsavelTratativa;
    private Usuario responsavelNc;
    private Empresa empresa;

    @BeforeEach
    void setup() {
        empresa = new Empresa();
        empresa.setId(UUID.randomUUID());
        empresa.setRazaoSocial("Construtora ABC");

        estabelecimento = new Estabelecimento();
        estabelecimento.setId(UUID.randomUUID());
        estabelecimento.setNome("Obra Alpha");
        estabelecimento.setCodigo("OBR-001");

        responsavelTratativa = new Usuario();
        responsavelTratativa.setId(UUID.randomUUID());
        responsavelTratativa.setEmail("tratativa@construtora.com");
        responsavelTratativa.setEmpresa(empresa);

        responsavelNc = new Usuario();
        responsavelNc.setId(UUID.randomUUID());
        responsavelNc.setEmail("eng@construtora.com");
        responsavelNc.setEmpresa(empresa);

        nc = new NaoConformidade();
        nc.setId(UUID.randomUUID());
        nc.setTitulo("NC Teste");
        nc.setEstabelecimento(estabelecimento);
        nc.setResponsavelTratativa(responsavelTratativa);
        nc.setResponsavelNc(responsavelNc);
    }

    @Test
    void ao_abrir_envia_templateA_para_dinamicos_e_padrao() {
        UUID estId = estabelecimento.getId();
        UUID empId = empresa.getId();

        EmailPadrao padrao = new EmailPadrao();
        padrao.setEmail("diretor@empresa.com");

        when(ncRepository.findById(nc.getId())).thenReturn(Optional.of(nc));
        when(emailPadraoRepository.findByEstabelecimentoIdAndEmpresaId(estId, empId))
                .thenReturn(List.of(padrao));

        NcEmailEvent event = new NcEmailEvent(this, nc.getId(),
                null, StatusNaoConformidade.ABERTA, List.of(), List.of(), null);
        listener.onNcEmail(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
        verify(sender).enviarTemplateA(eq(nc), eq(StatusNaoConformidade.ABERTA), captor.capture());
        assertThat(captor.getValue()).contains("eng@construtora.com", "diretor@empresa.com");
    }

    @Test
    void email_padrao_que_coincide_com_dinamico_nao_duplica() {
        UUID estId = estabelecimento.getId();
        UUID empId = empresa.getId();

        EmailPadrao padrao = new EmailPadrao();
        padrao.setEmail("eng@construtora.com");

        when(ncRepository.findById(nc.getId())).thenReturn(Optional.of(nc));
        when(emailPadraoRepository.findByEstabelecimentoIdAndEmpresaId(estId, empId))
                .thenReturn(List.of(padrao));

        NcEmailEvent event = new NcEmailEvent(this, nc.getId(),
                null, StatusNaoConformidade.ABERTA, List.of(), List.of(), null);
        listener.onNcEmail(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
        verify(sender).enviarTemplateA(any(), any(), captor.capture());
        assertThat(captor.getValue()).containsExactly("eng@construtora.com");
    }

    @Test
    void email_padrao_excluido_nao_e_enviado() {
        UUID estId = estabelecimento.getId();
        UUID empId = empresa.getId();

        EmailPadrao padrao = new EmailPadrao();
        padrao.setEmail("excluido@empresa.com");

        when(ncRepository.findById(nc.getId())).thenReturn(Optional.of(nc));
        when(emailPadraoRepository.findByEstabelecimentoIdAndEmpresaId(estId, empId))
                .thenReturn(List.of(padrao));

        NcEmailEvent event = new NcEmailEvent(this, nc.getId(),
                null, StatusNaoConformidade.ABERTA, List.of(),
                List.of("excluido@empresa.com"), null);
        listener.onNcEmail(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
        verify(sender).enviarTemplateA(any(), any(), captor.capture());
        assertThat(captor.getValue()).doesNotContain("excluido@empresa.com");
    }

    @Test
    void outras_transicoes_enviam_templateB_somente_para_dinamicos_sem_consultar_padrao() {
        when(ncRepository.findById(nc.getId())).thenReturn(Optional.of(nc));

        NcEmailEvent event = new NcEmailEvent(this, nc.getId(),
                StatusNaoConformidade.ABERTA,
                StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO,
                List.of("manual@empresa.com"), List.of(), "investigação submetida");
        listener.onNcEmail(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
        verify(sender).enviarTemplateB(any(), any(), any(), captor.capture(), eq("investigação submetida"));
        verify(emailPadraoRepository, never()).findByEstabelecimentoIdAndEmpresaId(any(), any());
        assertThat(captor.getValue()).contains("eng@construtora.com", "manual@empresa.com");
    }

    @Test
    void quandoBuilderRetornaEvento_publicaNoKafka() {
        NcKafkaEvent kafkaEvent = new NcKafkaEvent(UUID.randomUUID(), "NC_ATIVADA", nc.getId(),
                List.of(responsavelTratativa.getId()), "EngSeg — NC Teste", "corpo");
        when(ncRepository.findById(nc.getId())).thenReturn(Optional.of(nc));
        when(pushMessageBuilder.resolver(nc, StatusNaoConformidade.ABERTA,
                StatusNaoConformidade.AGUARDANDO_TRATATIVA, null)).thenReturn(kafkaEvent);

        NcEmailEvent event = new NcEmailEvent(this, nc.getId(),
                StatusNaoConformidade.ABERTA, StatusNaoConformidade.AGUARDANDO_TRATATIVA,
                List.of(), List.of(), null);
        listener.onNcEmail(event);

        verify(kafkaTemplate, times(1)).send(eq("engseg.nc.events"), eq(kafkaEvent));
    }

    @Test
    void quandoBuilderRetornaNull_naoPublicaNoKafka() {
        when(ncRepository.findById(nc.getId())).thenReturn(Optional.of(nc));
        when(pushMessageBuilder.resolver(any(), any(), any(), any())).thenReturn(null);

        NcEmailEvent event = new NcEmailEvent(this, nc.getId(),
                StatusNaoConformidade.ABERTA, StatusNaoConformidade.EM_TRATAMENTO,
                List.of(), List.of(), null);
        listener.onNcEmail(event);

        verify(kafkaTemplate, never()).send(any(), any());
    }
}
