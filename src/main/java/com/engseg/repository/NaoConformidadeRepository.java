package com.engseg.repository;

import com.engseg.entity.NaoConformidade;
import com.engseg.entity.StatusNaoConformidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface NaoConformidadeRepository extends JpaRepository<NaoConformidade, UUID> {

    List<NaoConformidade> findByStatus(StatusNaoConformidade status);

    List<NaoConformidade> findByEstabelecimentoId(UUID estabelecimentoId);

    List<NaoConformidade> findByStatusAndEstabelecimentoId(StatusNaoConformidade status, UUID estabelecimentoId);

    List<NaoConformidade> findByEstabelecimentoIdIn(Collection<UUID> estabelecimentoIds);

    List<NaoConformidade> findByStatusAndEstabelecimentoIdIn(StatusNaoConformidade status, Collection<UUID> estabelecimentoIds);

    List<NaoConformidade> findByEstabelecimento_EmpresaId(UUID empresaId);

    List<NaoConformidade> findByStatusAndEstabelecimento_EmpresaId(StatusNaoConformidade status, UUID empresaId);

    @Query("SELECT nc FROM NaoConformidade nc WHERE nc.vencida = 'N' AND nc.status != 'CONCLUIDO' AND nc.dataLimiteResolucao < :today")
    List<NaoConformidade> findVencidas(@Param("today") LocalDate today);

    long countByStatus(StatusNaoConformidade status);

    long countByRegraDeOuro(boolean regraDeOuro);

    @Query("SELECT COUNT(nc) FROM NaoConformidade nc JOIN nc.normas norma WHERE norma.id = :normaId")
    long countByNormaId(@Param("normaId") UUID normaId);

    @Query("SELECT COUNT(nc) FROM NaoConformidade nc JOIN nc.normas norma " +
           "WHERE norma.id = :normaId AND nc.status <> com.engseg.entity.StatusNaoConformidade.CONCLUIDO")
    long countAtivasByNormaId(@Param("normaId") UUID normaId);

    List<NaoConformidade> findByNcAnteriorId(UUID ncAnteriorId);

    List<NaoConformidade> findTop10ByOrderByDataRegistroDesc();

    @Query("SELECT n FROM NaoConformidade n WHERE n.dataLimiteResolucao = :data " +
           "AND n.status <> com.engseg.entity.StatusNaoConformidade.CONCLUIDO")
    List<NaoConformidade> findAtivasByDataLimiteResolucao(
            @Param("data") java.time.LocalDate data);

    List<NaoConformidade> findByEmpresaContratadaId(UUID empresaContratadaId);

    long countByEmpresaContratadaId(UUID empresaContratadaId);

    long countByStatusAndEmpresaContratadaId(StatusNaoConformidade status, UUID empresaContratadaId);

    long countByRegraDeOuroAndEmpresaContratadaId(boolean regraDeOuro, UUID empresaContratadaId);

    @Query("SELECT COUNT(nc) FROM NaoConformidade nc WHERE " +
           "(:empresaContratadaId IS NULL OR nc.empresaContratada.id = :empresaContratadaId) AND " +
           "(:estabelecimentoId IS NULL OR nc.estabelecimento.id = :estabelecimentoId) AND " +
           "(:empresaId IS NULL OR nc.estabelecimento.empresa.id = :empresaId)")
    long countFiltered(@Param("empresaContratadaId") UUID empresaContratadaId,
                       @Param("estabelecimentoId") UUID estabelecimentoId,
                       @Param("empresaId") UUID empresaId);

    @Query("SELECT COUNT(nc) FROM NaoConformidade nc WHERE nc.status = :status AND " +
           "(:empresaContratadaId IS NULL OR nc.empresaContratada.id = :empresaContratadaId) AND " +
           "(:estabelecimentoId IS NULL OR nc.estabelecimento.id = :estabelecimentoId) AND " +
           "(:empresaId IS NULL OR nc.estabelecimento.empresa.id = :empresaId)")
    long countByStatusFiltered(@Param("status") StatusNaoConformidade status,
                               @Param("empresaContratadaId") UUID empresaContratadaId,
                               @Param("estabelecimentoId") UUID estabelecimentoId,
                               @Param("empresaId") UUID empresaId);

    @Query("SELECT COUNT(nc) FROM NaoConformidade nc WHERE nc.regraDeOuro = :regraDeOuro AND " +
           "(:empresaContratadaId IS NULL OR nc.empresaContratada.id = :empresaContratadaId) AND " +
           "(:estabelecimentoId IS NULL OR nc.estabelecimento.id = :estabelecimentoId) AND " +
           "(:empresaId IS NULL OR nc.estabelecimento.empresa.id = :empresaId)")
    long countByRegraDeOuroFiltered(@Param("regraDeOuro") boolean regraDeOuro,
                                    @Param("empresaContratadaId") UUID empresaContratadaId,
                                    @Param("estabelecimentoId") UUID estabelecimentoId,
                                    @Param("empresaId") UUID empresaId);

    @Query("SELECT nc FROM NaoConformidade nc WHERE " +
           "nc.dataRegistro >= :dataInicio AND " +
           "nc.dataRegistro <= :dataFim AND " +
           "(:estabelecimentoId IS NULL OR nc.estabelecimento.id = :estabelecimentoId) AND " +
           "(:empresaContratadaId IS NULL OR nc.empresaContratada.id = :empresaContratadaId) AND " +
           "(:status IS NULL OR nc.status = :status) " +
           "ORDER BY nc.dataRegistro DESC")
    List<NaoConformidade> findParaRelatorio(
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim,
        @Param("estabelecimentoId") UUID estabelecimentoId,
        @Param("empresaContratadaId") UUID empresaContratadaId,
        @Param("status") StatusNaoConformidade status);

    @Query("SELECT nc FROM NaoConformidade nc WHERE " +
           "nc.dataLimiteResolucao IS NOT NULL AND " +
           "nc.dataLimiteResolucao <= :dataLimite AND " +
           "nc.status <> com.engseg.entity.StatusNaoConformidade.CONCLUIDO AND " +
           "(:estabelecimentoId IS NULL OR nc.estabelecimento.id = :estabelecimentoId) AND " +
           "(:empresaContratadaId IS NULL OR nc.empresaContratada.id = :empresaContratadaId) " +
           "ORDER BY nc.dataLimiteResolucao ASC")
    List<NaoConformidade> findVencidasOuAVencer(
        @Param("dataLimite") LocalDate dataLimite,
        @Param("estabelecimentoId") UUID estabelecimentoId,
        @Param("empresaContratadaId") UUID empresaContratadaId);
}
