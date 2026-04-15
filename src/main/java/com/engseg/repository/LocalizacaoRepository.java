package com.engseg.repository;

import com.engseg.entity.Localizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface LocalizacaoRepository extends JpaRepository<Localizacao, UUID> {
    List<Localizacao> findByEstabelecimentoId(UUID estabelecimentoId);
    List<Localizacao> findAllByAtivo(boolean ativo);
    List<Localizacao> findByEstabelecimento_EmpresaId(UUID empresaId);
}
