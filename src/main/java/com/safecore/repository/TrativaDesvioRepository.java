package com.safecore.repository;

import com.safecore.entity.StatusTratativaDesvio;
import com.safecore.entity.TrativaDesvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrativaDesvioRepository extends JpaRepository<TrativaDesvio, UUID> {
    List<TrativaDesvio> findByDesvioIdOrderByNumeroAsc(UUID desvioId);
    List<TrativaDesvio> findByDesvioIdAndStatus(UUID desvioId, StatusTratativaDesvio status);
    long countByDesvioId(UUID desvioId);

    @Query("SELECT COALESCE(MAX(t.rodada), 0) FROM TrativaDesvio t WHERE t.desvio.id = :desvioId")
    Optional<Integer> findMaxRodadaByDesvioId(@Param("desvioId") UUID desvioId);
}
