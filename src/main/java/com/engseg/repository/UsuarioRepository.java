package com.engseg.repository;

import com.engseg.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Usuario> findAllByAtivo(boolean ativo);
    List<Usuario> findAllByEmpresaIdAndAtivo(UUID empresaId, boolean ativo);
    List<Usuario> findAllByEmpresaId(UUID empresaId);
}
