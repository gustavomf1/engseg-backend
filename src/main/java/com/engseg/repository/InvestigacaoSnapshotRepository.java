package com.engseg.repository;

import com.engseg.entity.InvestigacaoSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestigacaoSnapshotRepository extends JpaRepository<InvestigacaoSnapshot, UUID> {
    List<InvestigacaoSnapshot> findByNaoConformidadeIdOrderByDataSubmissaoAsc(UUID ncId);
    Optional<InvestigacaoSnapshot> findFirstByNaoConformidadeIdAndStatusOrderByDataSubmissaoDesc(UUID ncId, String status);
}
