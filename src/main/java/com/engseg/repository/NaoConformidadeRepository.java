package com.engseg.repository;

import com.engseg.entity.NaoConformidade;
import com.engseg.entity.StatusNaoConformidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface NaoConformidadeRepository extends JpaRepository<NaoConformidade, UUID> {

    List<NaoConformidade> findByStatus(StatusNaoConformidade status);

    List<NaoConformidade> findByEstabelecimentoId(UUID estabelecimentoId);

    List<NaoConformidade> findByStatusAndEstabelecimentoId(StatusNaoConformidade status, UUID estabelecimentoId);

    @Query("SELECT nc FROM NaoConformidade nc WHERE nc.vencida = 'N' AND nc.status != 'CONCLUIDO' AND nc.dataLimiteResolucao < :today")
    List<NaoConformidade> findVencidas(@Param("today") LocalDate today);

    long countByStatus(StatusNaoConformidade status);

    long countByRegraDeOuro(boolean regraDeOuro);
}
