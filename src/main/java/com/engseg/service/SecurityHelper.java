package com.engseg.service;

import com.engseg.entity.PerfilUsuario;
import com.engseg.entity.Usuario;
import com.engseg.repository.EstabelecimentoEmpresaRepository;
import com.engseg.repository.UsuarioRepository;
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

    /**
     * Retorna os estabelecimentos da empresa EXTERNO (via junction table).
     * Apenas deve ser chamado quando isExterno() == true.
     */
    public List<UUID> getEstabelecimentosDoExterno() {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        return estabelecimentoEmpresaRepository
                .findByEmpresaIdAndAtivo(empresaId, true)
                .stream()
                .map(ee -> ee.getEstabelecimento().getId())
                .toList();
    }
}
