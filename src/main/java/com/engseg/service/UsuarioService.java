package com.engseg.service;

import com.engseg.dto.request.CriarUsuarioDiretoRequest;
import com.engseg.dto.request.UsuarioRequest;
import com.engseg.dto.response.UsuarioResponse;
import com.engseg.entity.PerfilUsuario;
import com.engseg.entity.Usuario;
import com.engseg.exception.BusinessException;
import com.engseg.exception.ResourceNotFoundException;
import com.engseg.repository.EmpresaRepository;
import com.engseg.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioResponse> findAll(Boolean ativo, UUID empresaId) {
        List<Usuario> items;
        if (empresaId != null && ativo != null) {
            items = usuarioRepository.findAllByEmpresaIdAndAtivo(empresaId, ativo);
        } else if (empresaId != null) {
            items = usuarioRepository.findAllByEmpresaId(empresaId);
        } else if (ativo != null) {
            items = usuarioRepository.findAllByAtivo(ativo);
        } else {
            items = usuarioRepository.findAll();
        }
        return items.stream()
                .map(this::toResponse)
                .toList();
    }

    public UsuarioResponse findById(UUID id) {
        return usuarioRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }

    @Transactional
    public UsuarioResponse create(UsuarioRequest request) {
        var empresa = empresaRepository.findById(request.empresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + request.empresaId()));

        if (request.senha() == null || request.senha().isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória ao criar usuário");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(request.perfil())
                .empresa(empresa)
                .telefone(request.telefone())
                .ativo(true)
                .dtCriacao(LocalDate.now())
                .build();

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse update(UUID id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        var empresa = empresaRepository.findById(request.empresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + request.empresaId()));

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setPerfil(request.perfil());
        usuario.setEmpresa(empresa);
        usuario.setTelefone(request.telefone());

        if (request.senha() != null && !request.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(request.senha()));
        }

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void delete(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
        usuario.setAtivo(false);
        usuario.setDtInativacao(LocalDate.now());
        usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioResponse reativar(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
        usuario.setAtivo(true);
        usuario.setDtInativacao(null);
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse criarDireto(CriarUsuarioDiretoRequest request) {
        var empresa = empresaRepository.findById(request.empresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + request.empresaId()));

        if (request.isAdmin() && request.perfil() != PerfilUsuario.ENGENHEIRO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "isAdmin só é permitido para perfil ENGENHEIRO");
        }

        var usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(request.perfil())
                .empresa(empresa)
                .admin(request.isAdmin())
                .dtCriacao(LocalDate.now())
                .build();

        return toResponse(usuarioRepository.save(usuario));
    }

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getPerfil(),
                u.getEmpresa().getId(),
                u.getEmpresa().getRazaoSocial(),
                u.getEmpresa().getCnpj(),
                u.getTelefone(),
                u.isAtivo(),
                u.getDtCriacao(),
                u.getDtInativacao()
        );
    }
}
