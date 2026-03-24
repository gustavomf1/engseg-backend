package com.engseg.repository;

import com.engseg.entity.Norma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NormaRepository extends JpaRepository<Norma, UUID> {
    List<Norma> findAllByAtivo(boolean ativo);
}
