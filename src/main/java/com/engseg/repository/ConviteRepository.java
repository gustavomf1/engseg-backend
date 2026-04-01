package com.engseg.repository;

import com.engseg.entity.ConviteUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConviteRepository extends JpaRepository<ConviteUsuario, UUID> {
}
