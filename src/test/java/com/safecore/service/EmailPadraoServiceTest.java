package com.safecore.service;

import com.safecore.entity.*;
import com.safecore.repository.EmailPadraoRepository;
import com.safecore.repository.EmpresaRepository;
import com.safecore.repository.EstabelecimentoRepository;
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
    void listar_retorna_emails_do_par() {
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

        when(repository.findByEstabelecimentoIdAndEmpresaId(estId, empId))
                .thenReturn(List.of(e));

        var result = service.listar(estId, empId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).email()).isEqualTo("diretor@empresa.com");
    }
}
