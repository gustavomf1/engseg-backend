package com.engseg.repository;

import com.engseg.entity.EmailPadraoNc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmailPadraoNcRepository extends JpaRepository<EmailPadraoNc, UUID> {
    List<EmailPadraoNc> findByEstabelecimentoIdAndEmpresaId(UUID estabelecimentoId, UUID empresaId);
    boolean existsByEstabelecimentoIdAndEmpresaIdAndEmail(UUID estabelecimentoId, UUID empresaId, String email);
}
