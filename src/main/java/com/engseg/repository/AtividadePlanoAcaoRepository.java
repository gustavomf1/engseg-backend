package com.engseg.repository;

import com.engseg.entity.AtividadePlanoAcao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AtividadePlanoAcaoRepository extends JpaRepository<AtividadePlanoAcao, UUID> {
    List<AtividadePlanoAcao> findByNaoConformidadeIdOrderByOrdem(UUID naoConformidadeId);
}
