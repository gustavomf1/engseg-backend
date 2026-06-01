package com.engseg.service;

import com.engseg.entity.SenhaResetToken;
import com.engseg.entity.Usuario;
import com.engseg.exception.BusinessException;
import com.engseg.repository.SenhaResetTokenRepository;
import com.engseg.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SenhaResetServiceTest {

    @Mock SenhaResetTokenRepository tokenRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock PasswordResetEmailSender emailSender;
    @InjectMocks SenhaResetService senhaResetService;

    private Usuario buildUsuario() {
        return Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Gustavo")
                .email("gustavo@ers.com.br")
                .senha("hash-antiga")
                .build();
    }

    @Test
    void solicitarReset_emailNaoExiste_naoEnviaEmailNemSalva() {
        when(usuarioRepository.findByEmail("naoexiste@x.com")).thenReturn(Optional.empty());

        senhaResetService.solicitarReset("naoexiste@x.com");

        verify(emailSender, never()).enviar(any(), any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void solicitarReset_emailExiste_salvaTokenEEnviaEmail() {
        var usuario = buildUsuario();
        when(usuarioRepository.findByEmail("gustavo@ers.com.br")).thenReturn(Optional.of(usuario));
        when(tokenRepository.findByUsuarioIdAndUsadoFalse(usuario.getId())).thenReturn(Collections.emptyList());
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        senhaResetService.solicitarReset("gustavo@ers.com.br");

        verify(tokenRepository).save(any(SenhaResetToken.class));
        verify(emailSender).enviar(eq("gustavo@ers.com.br"), any());
    }

    @Test
    void solicitarReset_tokensAtivosExistem_invalidaAntesDecriar() {
        var usuario = buildUsuario();
        var tokenAntigo = SenhaResetToken.builder()
                .id(UUID.randomUUID()).usuario(usuario)
                .otp("111111").otpExpiresAt(LocalDateTime.now().plusMinutes(10))
                .usado(false).createdAt(LocalDateTime.now()).build();
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.of(usuario));
        when(tokenRepository.findByUsuarioIdAndUsadoFalse(any())).thenReturn(List.of(tokenAntigo));
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        senhaResetService.solicitarReset("gustavo@ers.com.br");

        assertThat(tokenAntigo.isUsado()).isTrue();
    }

    @Test
    void verificarOtp_emailNaoExiste_lancaBusinessException() {
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> senhaResetService.verificarOtp("x@x.com", "123456"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void verificarOtp_otpInvalido_lancaBusinessException() {
        var usuario = buildUsuario();
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.of(usuario));
        when(tokenRepository.findByUsuarioIdAndOtpAndUsadoFalseAndOtpExpiresAtAfter(any(), any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> senhaResetService.verificarOtp("gustavo@ers.com.br", "000000"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void verificarOtp_otpValido_retornaResetTokenEPersiste() {
        var usuario = buildUsuario();
        var token = SenhaResetToken.builder()
                .id(UUID.randomUUID()).usuario(usuario)
                .otp("418290").otpExpiresAt(LocalDateTime.now().plusMinutes(10))
                .usado(false).createdAt(LocalDateTime.now()).build();
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.of(usuario));
        when(tokenRepository.findByUsuarioIdAndOtpAndUsadoFalseAndOtpExpiresAtAfter(any(), any(), any()))
                .thenReturn(Optional.of(token));
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UUID resetToken = senhaResetService.verificarOtp("gustavo@ers.com.br", "418290");

        assertThat(resetToken).isNotNull();
        assertThat(token.getResetToken()).isEqualTo(resetToken);
        assertThat(token.getResetTokenExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void redefinirSenha_resetTokenInvalido_lancaBusinessException() {
        when(tokenRepository.findByResetTokenAndUsadoFalseAndResetTokenExpiresAtAfter(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> senhaResetService.redefinirSenha(UUID.randomUUID(), "Senha@123"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void redefinirSenha_senhaFraca_lancaBusinessException() {
        var usuario = buildUsuario();
        var token = SenhaResetToken.builder()
                .id(UUID.randomUUID()).usuario(usuario)
                .resetToken(UUID.randomUUID())
                .resetTokenExpiresAt(LocalDateTime.now().plusMinutes(5))
                .usado(false).createdAt(LocalDateTime.now()).build();
        when(tokenRepository.findByResetTokenAndUsadoFalseAndResetTokenExpiresAtAfter(any(), any()))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> senhaResetService.redefinirSenha(token.getResetToken(), "fraca"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("senha");
    }

    @Test
    void redefinirSenha_valido_atualizaSenhaEMarcaUsado() {
        var usuario = buildUsuario();
        var rt = UUID.randomUUID();
        var token = SenhaResetToken.builder()
                .id(UUID.randomUUID()).usuario(usuario)
                .resetToken(rt).resetTokenExpiresAt(LocalDateTime.now().plusMinutes(5))
                .usado(false).createdAt(LocalDateTime.now()).build();
        when(tokenRepository.findByResetTokenAndUsadoFalseAndResetTokenExpiresAtAfter(eq(rt), any()))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("Senha@123")).thenReturn("hash-nova");

        senhaResetService.redefinirSenha(rt, "Senha@123");

        assertThat(token.isUsado()).isTrue();
        assertThat(usuario.getSenha()).isEqualTo("hash-nova");
    }
}
