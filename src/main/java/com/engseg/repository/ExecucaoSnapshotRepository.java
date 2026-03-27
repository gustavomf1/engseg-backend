package com.engseg.repository;

import com.engseg.entity.ExecucaoSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExecucaoSnapshotRepository extends JpaRepository<ExecucaoSnapshot, UUID> {
    List<ExecucaoSnapshot> findByNaoConformidadeIdOrderByDataSubmissaoAsc(UUID ncId);
    Optional<ExecucaoSnapshot> findFirstByNaoConformidadeIdAndStatusOrderByDataSubmissaoDesc(UUID ncId, String status);
}
