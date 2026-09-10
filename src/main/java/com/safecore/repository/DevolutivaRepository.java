package com.safecore.repository;

import com.safecore.entity.Devolutiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface DevolutivaRepository extends JpaRepository<Devolutiva, UUID> {
}
