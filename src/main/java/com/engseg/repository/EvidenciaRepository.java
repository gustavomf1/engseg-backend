package com.engseg.repository;

import com.engseg.entity.Evidencia;
import com.engseg.entity.TipoEvidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface EvidenciaRepository extends JpaRepository<Evidencia, UUID> {
    List<Evidencia> findByNaoConformidadeId(UUID naoConformidadeId);
    List<Evidencia> findByNaoConformidadeIdAndTipoEvidencia(UUID naoConformidadeId, TipoEvidencia tipoEvidencia);
    List<Evidencia> findByDesvioId(UUID desvioId);
    List<Evidencia> findByDesvioIdAndTipoEvidencia(UUID desvioId, TipoEvidencia tipoEvidencia);
    List<Evidencia> findByExecucaoAcaoId(UUID execucaoAcaoId);
    List<Evidencia> findByExecucaoSnapshotId(UUID execucaoSnapshotId);
    List<Evidencia> findByNaoConformidadeIdAndTipoEvidenciaAndExecucaoSnapshotIsNull(UUID naoConformidadeId, TipoEvidencia tipoEvidencia);
}
