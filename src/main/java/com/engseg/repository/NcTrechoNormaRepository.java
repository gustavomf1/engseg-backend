package com.engseg.repository;

import com.engseg.entity.NcTrechoNorma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NcTrechoNormaRepository extends JpaRepository<NcTrechoNorma, UUID> {
    List<NcTrechoNorma> findByNaoConformidadeIdOrderByDataVinculoAsc(UUID naoConformidadeId);
}
