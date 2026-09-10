package com.safecore.repository;

import com.safecore.entity.HistoricoNc;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface HistoricoNcRepository extends JpaRepository<HistoricoNc, UUID> {
    List<HistoricoNc> findByNaoConformidadeIdOrderByDataAcaoAsc(UUID naoConformidadeId);
}
