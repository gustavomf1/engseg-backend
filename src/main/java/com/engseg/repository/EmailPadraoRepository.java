package com.engseg.repository;

import com.engseg.entity.EmailPadrao;
import com.engseg.entity.TipoEmailPadrao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmailPadraoRepository extends JpaRepository<EmailPadrao, UUID> {

    @EntityGraph(attributePaths = {"estabelecimento", "empresa"})
    List<EmailPadrao> findByEstabelecimentoIdAndEmpresaIdAndTipo(
            UUID estabelecimentoId, UUID empresaId, TipoEmailPadrao tipo);

    boolean existsByEstabelecimentoIdAndEmpresaIdAndEmailAndTipo(
            UUID estabelecimentoId, UUID empresaId, String email, TipoEmailPadrao tipo);
}
