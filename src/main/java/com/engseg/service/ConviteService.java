package com.engseg.service;

import com.engseg.dto.request.ConviteRequest;
import com.engseg.dto.request.RegistrarViaConviteRequest;
import com.engseg.dto.response.ConviteResponse;
import com.engseg.entity.ConviteUsuario;
import com.engseg.entity.Usuario;
import com.engseg.exception.BusinessException;
import com.engseg.exception.ResourceNotFoundException;
import com.engseg.repository.ConviteRepository;
import com.engseg.repository.EmpresaRepository;
import com.engseg.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConviteService {

    private final ConviteRepository conviteRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ConviteResponse criar(ConviteRequest request) {
        var empresa = empresaRepository.findById(request.empresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

        ConviteUsuario convite = ConviteUsuario.builder()
                .id(UUID.randomUUID())
                .empresa(empresa)
                .perfil(request.perfil())
                .expiresAt(LocalDateTime.now().plusMinutes(request.minutos()))
                .criadoEm(LocalDateTime.now())
                .build();

        conviteRepository.save(convite);

        return new ConviteResponse(
                convite.getId(),
                empresa.getRazaoSocial(),
                empresa.getCnpj(),
                convite.getPerfil(),
                convite.getExpiresAt()
        );
    }

    public ConviteResponse buscar(UUID token) {
        ConviteUsuario convite = conviteRepository.findById(token)
                .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado ou inválido"));

        if (convite.isUsado()) {
            throw new BusinessException("Este convite já foi utilizado");
        }
        if (LocalDateTime.now().isAfter(convite.getExpiresAt())) {
            throw new BusinessException("Este convite expirou");
        }

        return new ConviteResponse(
                convite.getId(),
                convite.getEmpresa().getRazaoSocial(),
                convite.getEmpresa().getCnpj(),
                convite.getPerfil(),
                convite.getExpiresAt()
        );
    }

    @Transactional
    public void registrar(UUID token, RegistrarViaConviteRequest request) {
        ConviteUsuario convite = conviteRepository.findById(token)
                .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado ou inválido"));

        if (convite.isUsado()) {
            throw new BusinessException("Este convite já foi utilizado");
        }
        if (LocalDateTime.now().isAfter(convite.getExpiresAt())) {
            throw new BusinessException("Este convite expirou");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(convite.getPerfil())
                .empresa(convite.getEmpresa())
                .telefone(request.telefone())
                .ativo(true)
                .dtCriacao(LocalDate.now())
                .build();

        usuarioRepository.save(usuario);

        convite.setUsado(true);
        conviteRepository.save(convite);
    }
}
