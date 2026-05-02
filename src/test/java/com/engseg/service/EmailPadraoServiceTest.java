package com.engseg.service;

import com.engseg.entity.*;
import com.engseg.repository.EmailPadraoRepository;
import com.engseg.repository.EmpresaRepository;
import com.engseg.repository.EstabelecimentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailPadraoServiceTest {

    @Mock EmailPadraoRepository repository;
    @Mock EstabelecimentoRepository estabelecimentoRepository;
    @Mock EmpresaRepository empresaRepository;

    @InjectMocks EmailPadraoService service;

    @Test
    void listar_retorna_emails_do_par_filtrado_por_tipo() {
        UUID estId = UUID.randomUUID();
        UUID empId = UUID.randomUUID();

        Estabelecimento est = new Estabelecimento();
        est.setId(estId);
        est.setNome("Obra Alpha");
        est.setCodigo("OBR-001");

        Empresa emp = new Empresa();
        emp.setId(empId);
        emp.setRazaoSocial("Construtora ABC");

        EmailPadrao e = new EmailPadrao();
        e.setId(UUID.randomUUID());
        e.setEstabelecimento(est);
        e.setEmpresa(emp);
        e.setEmail("diretor@empresa.com");
        e.setDescricao("Diretor");
        e.setTipo(TipoEmailPadrao.NC);

        when(repository.findByEstabelecimentoIdAndEmpresaIdAndTipo(estId, empId, TipoEmailPadrao.NC))
                .thenReturn(List.of(e));

        var result = service.listar(estId, empId, TipoEmailPadrao.NC);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).email()).isEqualTo("diretor@empresa.com");
        assertThat(result.get(0).tipo()).isEqualTo(TipoEmailPadrao.NC);
    }
}
