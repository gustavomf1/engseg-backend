package com.engseg.repository;

import com.engseg.entity.EstabelecimentoEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstabelecimentoEmpresaRepository extends JpaRepository<EstabelecimentoEmpresa, UUID> {
    List<EstabelecimentoEmpresa> findByEstabelecimentoIdAndAtivo(UUID estabelecimentoId, boolean ativo);
    List<EstabelecimentoEmpresa> findByEmpresaIdAndAtivo(UUID empresaId, boolean ativo);
    Optional<EstabelecimentoEmpresa> findByEstabelecimentoIdAndEmpresaId(UUID estabelecimentoId, UUID empresaId);
    boolean existsByEstabelecimentoIdAndEmpresaId(UUID estabelecimentoId, UUID empresaId);

    @Query("SELECT ee FROM EstabelecimentoEmpresa ee JOIN FETCH ee.estabelecimento JOIN FETCH ee.empresa WHERE ee.ativo = true ORDER BY ee.estabelecimento.nome, ee.empresa.nomeFantasia, ee.empresa.razaoSocial")
    List<EstabelecimentoEmpresa> findAllAtivoFetched();
}
