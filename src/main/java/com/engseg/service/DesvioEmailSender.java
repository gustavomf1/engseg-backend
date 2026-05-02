package com.engseg.service;

import com.engseg.entity.Desvio;
import com.engseg.entity.StatusDesvio;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DesvioEmailSender {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String from;

    public void enviarTemplateA(Desvio desvio, StatusDesvio statusNovo, Set<String> destinatarios) {
        String corHeader = statusNovo == StatusDesvio.AGUARDANDO_TRATATIVA ? "#2563eb" : "#16a34a";
        String labelStatus = statusNovo == StatusDesvio.AGUARDANDO_TRATATIVA ? "ABERTO" : "CONCLUÍDO";
        String empresaNome = resolverNomeEmpresa(desvio);

        // Escape HTML entities to prevent XSS
        String titulo = HtmlUtils.htmlEscape(desvio.getTitulo());
        String descricao = HtmlUtils.htmlEscape(desvio.getDescricao() != null ? desvio.getDescricao() : "");
        String estNome = HtmlUtils.htmlEscape(desvio.getEstabelecimento().getNome());
        String empresaNomeEscaped = HtmlUtils.htmlEscape(empresaNome);

        String html = String.format("""
                <html><body style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto">
                  <div style="background:%s;color:#fff;padding:24px;border-radius:8px 8px 0 0">
                    <h2 style="margin:0;font-size:18px">Desvio %s</h2>
                  </div>
                  <div style="background:#f9fafb;padding:24px;border:1px solid #e5e7eb;border-top:none">
                    <p style="margin:0 0 8px"><strong>Título:</strong> %s</p>
                    <p style="margin:0 0 8px"><strong>ID:</strong> %s</p>
                    <p style="margin:0 0 8px"><strong>Descrição:</strong> %s</p>
                    <p style="margin:0 0 8px"><strong>Estabelecimento:</strong> %s</p>
                    <p style="margin:0 0 24px"><strong>Empresa:</strong> %s</p>
                    <div style="text-align:center">
                      <a href="%s/tratativas/DESVIO/%s"
                         style="background:#1d4ed8;color:#fff;padding:12px 28px;text-decoration:none;border-radius:6px;font-size:14px;display:inline-block">
                        Acessar Desvio
                      </a>
                    </div>
                  </div>
                  <div style="background:#f3f4f6;padding:12px;text-align:center;font-size:12px;color:#6b7280;border-radius:0 0 8px 8px">
                    EngSeg — Sistema de Gestão de Segurança em Engenharia
                  </div>
                </body></html>
                """,
                corHeader, labelStatus,
                titulo, desvio.getId(), descricao,
                estNome, empresaNomeEscaped,
                frontendUrl, desvio.getId()
        );

        enviar(destinatarios, "Desvio " + labelStatus + " — " + titulo, html);
    }

    public void enviarTemplateB(Desvio desvio, StatusDesvio statusAnterior, StatusDesvio statusNovo,
                                Set<String> destinatarios, String comentario) {
        String labelAnterior = statusAnterior != null
                ? statusAnterior.name().replace("_", " ") : "—";
        String labelNovo = statusNovo.name().replace("_", " ");
        String blocoComentario = (comentario != null && !comentario.isBlank())
                ? "<p style=\"margin:0 0 8px\"><strong>Comentário:</strong> " + HtmlUtils.htmlEscape(comentario) + "</p>"
                : "";

        // Escape HTML entities to prevent XSS
        String titulo = HtmlUtils.htmlEscape(desvio.getTitulo());
        String descricao = HtmlUtils.htmlEscape(desvio.getDescricao() != null ? desvio.getDescricao() : "");
        String estNome = HtmlUtils.htmlEscape(desvio.getEstabelecimento().getNome());

        String html = String.format("""
                <html><body style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto">
                  <div style="background:#1e40af;color:#fff;padding:24px;border-radius:8px 8px 0 0">
                    <h2 style="margin:0;font-size:16px">%s → %s</h2>
                    <p style="margin:4px 0 0;font-size:13px;opacity:0.85">Desvio — status atualizado</p>
                  </div>
                  <div style="background:#f9fafb;padding:24px;border:1px solid #e5e7eb;border-top:none">
                    <p style="margin:0 0 8px"><strong>Título:</strong> %s</p>
                    <p style="margin:0 0 8px"><strong>ID:</strong> %s</p>
                    <p style="margin:0 0 8px"><strong>Descrição:</strong> %s</p>
                    <p style="margin:0 0 8px"><strong>Estabelecimento:</strong> %s</p>
                    %s
                    <div style="text-align:center;margin-top:24px">
                      <a href="%s/tratativas/DESVIO/%s"
                         style="background:#1d4ed8;color:#fff;padding:12px 28px;text-decoration:none;border-radius:6px;font-size:14px;display:inline-block">
                        Acessar Desvio
                      </a>
                    </div>
                  </div>
                  <div style="background:#f3f4f6;padding:12px;text-align:center;font-size:12px;color:#6b7280;border-radius:0 0 8px 8px">
                    EngSeg — Sistema de Gestão de Segurança em Engenharia
                  </div>
                </body></html>
                """,
                labelAnterior, labelNovo,
                titulo, desvio.getId(), descricao,
                estNome, blocoComentario,
                frontendUrl, desvio.getId()
        );

        enviar(destinatarios, "Desvio Atualizado — " + titulo, html);
    }

    private String resolverNomeEmpresa(Desvio desvio) {
        if (desvio.getResponsavelDesvio() == null) return "—";
        var emp = desvio.getResponsavelDesvio().getEmpresa();
        if (emp == null) return "—";
        String nomeEmpresa = emp.getNomeFantasia() != null ? emp.getNomeFantasia() : emp.getRazaoSocial();
        return HtmlUtils.htmlEscape(nomeEmpresa);
    }

    private void enviar(Set<String> destinatarios, String assunto, String html) {
        if (destinatarios.isEmpty()) return;
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(destinatarios.toArray(new String[0]));
            helper.setSubject(assunto);
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (MessagingException e) {
            log.error("Falha ao enviar email de Desvio para {} destinatários: {}", destinatarios.size(), e.getMessage(), e);
        }
    }
}
