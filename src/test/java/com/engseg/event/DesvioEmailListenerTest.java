package com.engseg.event;

import com.engseg.entity.*;
import com.engseg.event.kafka.DesvioKafkaEvent;
import com.engseg.repository.DesvioRepository;
import com.engseg.repository.EmailPadraoRepository;
import com.engseg.service.DesvioEmailSender;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class DesvioEmailListenerTest {

    @Mock DesvioRepository desvioRepository;
    @Mock EmailPadraoRepository emailPadraoRepository;
    @Mock DesvioEmailSender sender;
    @Mock KafkaTemplate<String, DesvioKafkaEvent> kafkaTemplate;
    @InjectMocks DesvioEmailListener listener;

    private Desvio desvio;
    private Estabelecimento estabelecimento;
    private Usuario responsavelDesvio;
    private Usuario responsavelTratativa;
    private Usuario usuarioCriacao;
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

        usuarioCriacao = new Usuario();
        usuarioCriacao.setId(UUID.randomUUID());
        usuarioCriacao.setEmail("criador@engseg.com");

        responsavelDesvio = new Usuario();
        responsavelDesvio.setId(UUID.randomUUID());
        responsavelDesvio.setEmail("resp.desvio@construtora.com");
        responsavelDesvio.setEmpresa(empresa);

        responsavelTratativa = new Usuario();
        responsavelTratativa.setId(UUID.randomUUID());
        responsavelTratativa.setEmail("resp.tratativa@construtora.com");

        desvio = new Desvio();
        desvio.setId(UUID.randomUUID());
        desvio.setTitulo("Desvio Teste");
        desvio.setEstabelecimento(estabelecimento);
        desvio.setUsuarioCriacao(usuarioCriacao);
        desvio.setResponsavelDesvio(responsavelDesvio);
        desvio.setResponsavelTratativa(responsavelTratativa);
    }

    @Test
    void abertura_envia_templateA_para_dinamicos_e_padrao() {
        EmailPadrao padrao = new EmailPadrao();
        padrao.setEmail("diretor@empresa.com");

        when(desvioRepository.findById(desvio.getId())).thenReturn(Optional.of(desvio));
        when(emailPadraoRepository.findByEstabelecimentoIdAndEmpresaId(
                estabelecimento.getId(), empresa.getId()))
                .thenReturn(List.of(padrao));

        DesvioEmailEvent event = new DesvioEmailEvent(this, desvio.getId(),
                null, StatusDesvio.AGUARDANDO_TRATATIVA, List.of(), List.of(), null, empresa.getId());
        listener.onDesvioEmail(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
        verify(sender).enviarTemplateA(eq(desvio), eq(StatusDesvio.AGUARDANDO_TRATATIVA), captor.capture());
        assertThat(captor.getValue()).contains(
                "criador@engseg.com", "resp.desvio@construtora.com", "diretor@empresa.com");
        assertThat(captor.getValue()).doesNotContain("resp.tratativa@construtora.com");
    }

    @Test
    void abertura_nao_envia_para_responsavel_tratativa() {
        when(desvioRepository.findById(desvio.getId())).thenReturn(Optional.of(desvio));
        when(emailPadraoRepository.findByEstabelecimentoIdAndEmpresaId(any(), any()))
                .thenReturn(List.of());

        DesvioEmailEvent event = new DesvioEmailEvent(this, desvio.getId(),
                null, StatusDesvio.AGUARDANDO_TRATATIVA, List.of(), List.of(), null, empresa.getId());
        listener.onDesvioEmail(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
        verify(sender).enviarTemplateA(any(), any(), captor.capture());
        assertThat(captor.getValue()).doesNotContain("resp.tratativa@construtora.com");
    }

    @Test
    void abertura_nao_envia_para_responsavel_tratativa_mesmo_quando_e_email_padrao() {
        // se o email do responsável pela tratativa também estiver cadastrado como padrão,
        // ainda assim não deve receber na abertura
        EmailPadrao padraoTratativa = new EmailPadrao();
        padraoTratativa.setEmail("resp.tratativa@construtora.com");

        when(desvioRepository.findById(desvio.getId())).thenReturn(Optional.of(desvio));
        when(emailPadraoRepository.findByEstabelecimentoIdAndEmpresaId(
                estabelecimento.getId(), empresa.getId()))
                .thenReturn(List.of(padraoTratativa));

        DesvioEmailEvent event = new DesvioEmailEvent(this, desvio.getId(),
                null, StatusDesvio.AGUARDANDO_TRATATIVA, List.of(), List.of(), null, empresa.getId());
        listener.onDesvioEmail(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
        verify(sender).enviarTemplateA(any(), any(), captor.capture());
        assertThat(captor.getValue()).doesNotContain("resp.tratativa@construtora.com");
    }

    @Test
    void transicao_para_aguardando_tratativa_envia_para_responsavel_tratativa() {
        when(desvioRepository.findById(desvio.getId())).thenReturn(Optional.of(desvio));

        DesvioEmailEvent event = new DesvioEmailEvent(this, desvio.getId(),
                StatusDesvio.ABERTO, StatusDesvio.AGUARDANDO_TRATATIVA,
                List.of(), List.of(), null, empresa.getId());
        listener.onDesvioEmail(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
        verify(sender).enviarTemplateB(any(), any(), any(), captor.capture(), any());
        assertThat(captor.getValue()).contains("resp.tratativa@construtora.com");
    }

    @Test
    void conclusao_envia_templateA_para_dinamicos_e_padrao() {
        EmailPadrao padrao = new EmailPadrao();
        padrao.setEmail("diretor@empresa.com");

        when(desvioRepository.findById(desvio.getId())).thenReturn(Optional.of(desvio));
        when(emailPadraoRepository.findByEstabelecimentoIdAndEmpresaId(
                estabelecimento.getId(), empresa.getId()))
                .thenReturn(List.of(padrao));

        DesvioEmailEvent event = new DesvioEmailEvent(this, desvio.getId(),
                StatusDesvio.AGUARDANDO_APROVACAO, StatusDesvio.CONCLUIDO, List.of(), List.of(), null, empresa.getId());
        listener.onDesvioEmail(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
        verify(sender).enviarTemplateA(eq(desvio), eq(StatusDesvio.CONCLUIDO), captor.capture());
        assertThat(captor.getValue()).contains("diretor@empresa.com");
    }

    @Test
    void email_padrao_excluido_nao_e_enviado() {
        EmailPadrao padrao = new EmailPadrao();
        padrao.setEmail("excluido@empresa.com");

        when(desvioRepository.findById(desvio.getId())).thenReturn(Optional.of(desvio));
        when(emailPadraoRepository.findByEstabelecimentoIdAndEmpresaId(
                estabelecimento.getId(), empresa.getId()))
                .thenReturn(List.of(padrao));

        DesvioEmailEvent event = new DesvioEmailEvent(this, desvio.getId(),
                null, StatusDesvio.AGUARDANDO_TRATATIVA, List.of(),
                List.of("excluido@empresa.com"), null, empresa.getId());
        listener.onDesvioEmail(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
        verify(sender).enviarTemplateA(any(), any(), captor.capture());
        assertThat(captor.getValue()).doesNotContain("excluido@empresa.com");
    }

    @Test
    void submeter_tratativa_envia_templateB_apenas_para_dinamicos() {
        when(desvioRepository.findById(desvio.getId())).thenReturn(Optional.of(desvio));

        DesvioEmailEvent event = new DesvioEmailEvent(this, desvio.getId(),
                StatusDesvio.AGUARDANDO_TRATATIVA, StatusDesvio.AGUARDANDO_APROVACAO,
                List.of("manual@empresa.com"), List.of(), null, empresa.getId());
        listener.onDesvioEmail(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
        verify(sender).enviarTemplateB(any(), any(), any(), captor.capture(), any());
        verifyNoInteractions(emailPadraoRepository);
        assertThat(captor.getValue()).contains("resp.desvio@construtora.com", "manual@empresa.com");
        assertThat(captor.getValue()).doesNotContain("diretor@empresa.com");
    }

    @Test
    void reprovar_envia_templateB_apenas_para_dinamicos() {
        when(desvioRepository.findById(desvio.getId())).thenReturn(Optional.of(desvio));

        DesvioEmailEvent event = new DesvioEmailEvent(this, desvio.getId(),
                StatusDesvio.AGUARDANDO_APROVACAO, StatusDesvio.AGUARDANDO_TRATATIVA,
                List.of(), List.of(), "Tratativa 1: motivo da reprovação", empresa.getId());
        listener.onDesvioEmail(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
        verify(sender).enviarTemplateB(eq(desvio),
                eq(StatusDesvio.AGUARDANDO_APROVACAO), eq(StatusDesvio.AGUARDANDO_TRATATIVA),
                captor.capture(), eq("Tratativa 1: motivo da reprovação"));
        verifyNoInteractions(emailPadraoRepository);
        assertThat(captor.getValue()).doesNotContain("diretor@empresa.com");
    }

    @Test
    void devePublicarKafkaDesvio_quandoEventoProcessado() {
        UUID desvioId = UUID.randomUUID();
        desvio.setId(desvioId);
        when(desvioRepository.findById(desvioId)).thenReturn(Optional.of(desvio));

        DesvioEmailEvent event = new DesvioEmailEvent(this, desvioId,
                null, StatusDesvio.AGUARDANDO_TRATATIVA,
                List.of(), List.of(), null, empresa.getId());
        listener.onDesvioEmail(event);

        verify(kafkaTemplate, times(1)).send(eq("engseg.desvio.events"), any(DesvioKafkaEvent.class));
    }
}
