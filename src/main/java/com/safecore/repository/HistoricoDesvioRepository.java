package com.safecore.repository;

import com.safecore.entity.HistoricoDesvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface HistoricoDesvioRepository extends JpaRepository<HistoricoDesvio, UUID> {
}
