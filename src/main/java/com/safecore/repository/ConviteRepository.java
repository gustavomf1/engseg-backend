package com.safecore.repository;

import com.safecore.entity.ConviteUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConviteRepository extends JpaRepository<ConviteUsuario, UUID> {
}
