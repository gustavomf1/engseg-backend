package com.engseg.repository;

import com.engseg.entity.Desvio;
import com.engseg.entity.StatusDesvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface DesvioRepository extends JpaRepository<Desvio, UUID> {

    long countByStatus(StatusDesvio status);

    long countByRegraDeOuro(boolean regraDeOuro);
}
