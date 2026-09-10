package com.safecore.repository;

import com.safecore.entity.NcTrechoNorma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NcTrechoNormaRepository extends JpaRepository<NcTrechoNorma, UUID> {
    List<NcTrechoNorma> findByNaoConformidadeIdOrderByDataVinculoAsc(UUID naoConformidadeId);
}
