package com.engseg.repository;

import com.engseg.entity.Estabelecimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface EstabelecimentoRepository extends JpaRepository<Estabelecimento, UUID> {
    List<Estabelecimento> findByEmpresaId(UUID empresaId);
}
