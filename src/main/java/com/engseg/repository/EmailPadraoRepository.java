package com.engseg.repository;

import com.engseg.entity.EmailPadrao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EmailPadraoRepository extends JpaRepository<EmailPadrao, UUID> {

    @EntityGraph(attributePaths = {"estabelecimento", "empresa"})
    List<EmailPadrao> findByEstabelecimentoIdAndEmpresaId(UUID estabelecimentoId, UUID empresaId);

    boolean existsByEstabelecimentoIdAndEmpresaIdAndEmail(UUID estabelecimentoId, UUID empresaId, String email);

    @Query("SELECT e FROM EmailPadrao e JOIN FETCH e.estabelecimento JOIN FETCH e.empresa ORDER BY e.estabelecimento.nome, e.empresa.nomeFantasia, e.empresa.razaoSocial")
    List<EmailPadrao> findAllFetched();
}
