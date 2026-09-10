package com.safecore.service;

import com.safecore.entity.PerfilUsuario;
import com.safecore.entity.Usuario;
import com.safecore.repository.EstabelecimentoEmpresaRepository;
import com.safecore.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityHelper {

    private final UsuarioRepository usuarioRepository;
    private final EstabelecimentoEmpresaRepository estabelecimentoEmpresaRepository;

    public Usuario getUsuarioLogado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado: " + email));
    }

    public boolean isExterno() {
        return getUsuarioLogado().getPerfil() == PerfilUsuario.EXTERNO;
    }

    public boolean isTecnico() {
        return getUsuarioLogado().getPerfil() == PerfilUsuario.TECNICO;
    }

    public boolean isAdmin() {
        return getUsuarioLogado().isAdmin();
    }

    public List<UUID> getEstabelecimentosDoExterno() {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        return estabelecimentoEmpresaRepository
                .findByEmpresaIdAndAtivo(empresaId, true)
                .stream()
                .map(ee -> ee.getEstabelecimento().getId())
                .toList();
    }
}
