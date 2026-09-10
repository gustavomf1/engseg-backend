package com.safecore.repository;

import com.safecore.entity.Norma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NormaRepository extends JpaRepository<Norma, UUID> {
    List<Norma> findAllByAtivo(boolean ativo);
}
