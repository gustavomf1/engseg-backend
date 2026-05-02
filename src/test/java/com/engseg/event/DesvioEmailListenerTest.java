package com.engseg.event;

import com.engseg.entity.*;
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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DesvioEmailListenerTest {

    @Mock DesvioRepository desvioRepository;
    @Mock EmailPadraoRepository emailPadraoRepository;
    @Mock DesvioEmailSender sender;
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
        when(emailPadraoRepository.findByEstabelecimentoIdAndEmpresaIdAndTipo(
                estabelecimento.getId(), empresa.getId(), TipoEmailPadrao.DESVIO))
                .thenReturn(List.of(padrao));

        DesvioEmailEvent event = new DesvioEmailEvent(this, desvio.getId(),
                null, StatusDesvio.AGUARDANDO_TRATATIVA, List.of(), List.of(), null);
        listener.onDesvioEmail(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
        verify(sender).enviarTemplateA(eq(desvio), eq(StatusDesvio.AGUARDANDO_TRATATIVA), captor.capture());
        assertThat(captor.getValue()).contains(
                "criador@engseg.com", "resp.desvio@construtora.com",
                "resp.tratativa@construtora.com", "diretor@empresa.com");
    }

    @Test
    void conclusao_envia_templateA_para_dinamicos_e_padrao() {
        EmailPadrao padrao = new EmailPadrao();
        padrao.setEmail("diretor@empresa.com");

        when(desvioRepository.findById(desvio.getId())).thenReturn(Optional.of(desvio));
        when(emailPadraoRepository.findByEstabelecimentoIdAndEmpresaIdAndTipo(
                estabelecimento.getId(), empresa.getId(), TipoEmailPadrao.DESVIO))
                .thenReturn(List.of(padrao));

        DesvioEmailEvent event = new DesvioEmailEvent(this, desvio.getId(),
                StatusDesvio.AGUARDANDO_APROVACAO, StatusDesvio.CONCLUIDO, List.of(), List.of(), null);
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
        when(emailPadraoRepository.findByEstabelecimentoIdAndEmpresaIdAndTipo(
                estabelecimento.getId(), empresa.getId(), TipoEmailPadrao.DESVIO))
                .thenReturn(List.of(padrao));

        DesvioEmailEvent event = new DesvioEmailEvent(this, desvio.getId(),
                null, StatusDesvio.AGUARDANDO_TRATATIVA, List.of(),
                List.of("excluido@empresa.com"), null);
        listener.onDesvioEmail(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
        verify(sender).enviarTemplateA(any(), any(), captor.capture());
        assertThat(captor.getValue()).doesNotContain("excluido@empresa.com");
    }

    @Test
    void submeter_tratativa_envia_templateB_sem_consultar_padrao() {
        when(desvioRepository.findById(desvio.getId())).thenReturn(Optional.of(desvio));

        DesvioEmailEvent event = new DesvioEmailEvent(this, desvio.getId(),
                StatusDesvio.AGUARDANDO_TRATATIVA, StatusDesvio.AGUARDANDO_APROVACAO,
                List.of("manual@empresa.com"), List.of(), null);
        listener.onDesvioEmail(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
        verify(sender).enviarTemplateB(any(), any(), any(), captor.capture(), any());
        verify(emailPadraoRepository, never()).findByEstabelecimentoIdAndEmpresaIdAndTipo(any(), any(), any());
        assertThat(captor.getValue()).contains("resp.desvio@construtora.com", "manual@empresa.com");
    }

    @Test
    void reprovar_envia_templateB_sem_consultar_padrao() {
        when(desvioRepository.findById(desvio.getId())).thenReturn(Optional.of(desvio));

        DesvioEmailEvent event = new DesvioEmailEvent(this, desvio.getId(),
                StatusDesvio.AGUARDANDO_APROVACAO, StatusDesvio.AGUARDANDO_TRATATIVA,
                List.of(), List.of(), "Tratativa 1: motivo da reprovação");
        listener.onDesvioEmail(event);

        verify(sender).enviarTemplateB(eq(desvio),
                eq(StatusDesvio.AGUARDANDO_APROVACAO), eq(StatusDesvio.AGUARDANDO_TRATATIVA),
                any(), eq("Tratativa 1: motivo da reprovação"));
        verify(emailPadraoRepository, never()).findByEstabelecimentoIdAndEmpresaIdAndTipo(any(), any(), any());
    }
}
