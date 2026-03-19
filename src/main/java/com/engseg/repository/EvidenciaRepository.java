package com.engseg.repository;

import com.engseg.entity.Evidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface EvidenciaRepository extends JpaRepository<Evidencia, UUID> {
}
