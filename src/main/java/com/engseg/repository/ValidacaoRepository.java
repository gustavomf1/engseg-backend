package com.engseg.repository;

import com.engseg.entity.Validacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ValidacaoRepository extends JpaRepository<Validacao, UUID> {
}
