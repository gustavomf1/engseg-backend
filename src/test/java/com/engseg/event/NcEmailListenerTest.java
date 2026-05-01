package com.engseg.event;

import com.engseg.entity.*;
import com.engseg.repository.EmailPadraoNcRepository;
import com.engseg.repository.NaoConformidadeRepository;
import com.engseg.service.NcEmailSender;
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
class NcEmailListenerTest {

    @Mock NaoConformidadeRepository ncRepository;
    @Mock EmailPadraoNcRepository emailPadraoNcRepository;
    @Mock NcEmailSender sender;
    @InjectMocks NcEmailListener listener;

    private NaoConformidade nc;
    private Estabelecimento estabelecimento;
    private Usuario engConstrutora;
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

        engConstrutora = new Usuario();
        engConstrutora.setId(UUID.randomUUID());
        engConstrutora.setEmail("eng@construtora.com");
        engConstrutora.setEmpresa(empresa);

        nc = new NaoConformidade();
        nc.setId(UUID.randomUUID());
        nc.setTitulo("NC Teste");
        nc.setEstabelecimento(estabelecimento);
        nc.setEngResponsavelConstrutora(engConstrutora);
    }

    @Test
    void ao_abrir_envia_templateA_para_dinamicos_e_padrao() {
        UUID estId = estabelecimento.getId();
        UUID empId = empresa.getId();

        EmailPadraoNc padrao = new EmailPadraoNc();
        padrao.setEmail("diretor@empresa.com");

        when(ncRepository.findById(nc.getId())).thenReturn(Optional.of(nc));
        when(emailPadraoNcRepository.findByEstabelecimentoIdAndEmpresaId(estId, empId))
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

        EmailPadraoNc padrao = new EmailPadraoNc();
        padrao.setEmail("eng@construtora.com");

        when(ncRepository.findById(nc.getId())).thenReturn(Optional.of(nc));
        when(emailPadraoNcRepository.findByEstabelecimentoIdAndEmpresaId(estId, empId))
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

        EmailPadraoNc padrao = new EmailPadraoNc();
        padrao.setEmail("excluido@empresa.com");

        when(ncRepository.findById(nc.getId())).thenReturn(Optional.of(nc));
        when(emailPadraoNcRepository.findByEstabelecimentoIdAndEmpresaId(estId, empId))
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
        verify(emailPadraoNcRepository, never()).findByEstabelecimentoIdAndEmpresaId(any(), any());
        assertThat(captor.getValue()).contains("eng@construtora.com", "manual@empresa.com");
    }
}
