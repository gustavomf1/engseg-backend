package com.engseg.repository;

import com.engseg.entity.Devolutiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface DevolutivaRepository extends JpaRepository<Devolutiva, UUID> {
}
