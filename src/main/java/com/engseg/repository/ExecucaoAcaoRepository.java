package com.engseg.repository;

import com.engseg.entity.ExecucaoAcao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ExecucaoAcaoRepository extends JpaRepository<ExecucaoAcao, UUID> {
}
