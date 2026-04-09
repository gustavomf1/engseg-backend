package com.engseg.repository;

import com.engseg.entity.ExecucaoSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExecucaoSnapshotRepository extends JpaRepository<ExecucaoSnapshot, UUID> {
    List<ExecucaoSnapshot> findByNaoConformidadeIdOrderByDataSubmissaoAsc(UUID ncId);
    Optional<ExecucaoSnapshot> findFirstByNaoConformidadeIdAndStatusOrderByDataSubmissaoDesc(UUID ncId, String status);

    @Query("SELECT s FROM ExecucaoSnapshot s JOIN s.evidencias e WHERE e.id = :evidenciaId")
    List<ExecucaoSnapshot> findByEvidenciasId(@Param("evidenciaId") UUID evidenciaId);
}
