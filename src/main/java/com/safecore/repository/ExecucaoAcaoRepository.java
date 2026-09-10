package com.safecore.repository;

import com.safecore.entity.ExecucaoAcao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ExecucaoAcaoRepository extends JpaRepository<ExecucaoAcao, UUID> {
}
