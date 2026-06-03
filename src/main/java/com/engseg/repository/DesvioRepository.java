package com.engseg.repository;

import com.engseg.entity.Desvio;
import com.engseg.entity.StatusDesvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface DesvioRepository extends JpaRepository<Desvio, UUID> {

    long countByStatus(StatusDesvio status);

    @Query("SELECT COUNT(d) FROM Desvio d WHERE " +
           "(:empresaContratadaId IS NULL OR d.empresaContratada.id = :empresaContratadaId) AND " +
           "(:estabelecimentoId IS NULL OR d.estabelecimento.id = :estabelecimentoId) AND " +
           "(:empresaId IS NULL OR d.estabelecimento.empresa.id = :empresaId)")
    long countFiltered(@Param("empresaContratadaId") UUID empresaContratadaId,
                       @Param("estabelecimentoId") UUID estabelecimentoId,
                       @Param("empresaId") UUID empresaId);

    @Query("SELECT COUNT(d) FROM Desvio d WHERE d.status = :status AND " +
           "(:empresaContratadaId IS NULL OR d.empresaContratada.id = :empresaContratadaId) AND " +
           "(:estabelecimentoId IS NULL OR d.estabelecimento.id = :estabelecimentoId) AND " +
           "(:empresaId IS NULL OR d.estabelecimento.empresa.id = :empresaId)")
    long countByStatusFiltered(@Param("status") StatusDesvio status,
                               @Param("empresaContratadaId") UUID empresaContratadaId,
                               @Param("estabelecimentoId") UUID estabelecimentoId,
                               @Param("empresaId") UUID empresaId);

    List<Desvio> findByEstabelecimentoIdIn(Collection<UUID> estabelecimentoIds);

    List<Desvio> findByEstabelecimentoId(UUID estabelecimentoId);

    List<Desvio> findByEstabelecimento_EmpresaId(UUID empresaId);

    List<Desvio> findTop10ByOrderByDataRegistroDesc();

    @Query("SELECT d FROM Desvio d WHERE " +
           "d.dataRegistro >= :dataInicio AND " +
           "d.dataRegistro <= :dataFim AND " +
           "(:estabelecimentoId IS NULL OR d.estabelecimento.id = :estabelecimentoId) AND " +
           "(:empresaContratadaId IS NULL OR d.empresaContratada.id = :empresaContratadaId) AND " +
           "(:status IS NULL OR d.status = :status) " +
           "ORDER BY d.dataRegistro DESC")
    List<Desvio> findParaRelatorio(
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim,
        @Param("estabelecimentoId") UUID estabelecimentoId,
        @Param("empresaContratadaId") UUID empresaContratadaId,
        @Param("status") StatusDesvio status);
}
