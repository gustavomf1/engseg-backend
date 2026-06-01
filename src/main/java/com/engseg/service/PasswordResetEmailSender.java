package com.engseg.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetEmailSender {

    private final BrevoEmailService brevoEmailService;

    public void enviar(String to, String otp) {
        String otpEsc = HtmlUtils.htmlEscape(otp);
        StringBuilder blocks = new StringBuilder();
        for (char d : otpEsc.toCharArray()) {
            blocks.append(String.format(
                "<span style=\"display:inline-flex;align-items:center;justify-content:center;" +
                "width:42px;height:52px;border-radius:10px;background:#fff;" +
                "border:1px solid #dfe3ea;font-size:26px;font-weight:800;color:#0f172a;" +
                "font-variant-numeric:tabular-nums;box-shadow:0 1px 2px rgba(31,41,80,.06);" +
                "margin:0 4px\">%c</span>", d));
        }

        String html = String.format("""
                <html><body style="font-family:Arial,Helvetica,sans-serif;background:#f5f6fb;margin:0;padding:28px 20px">
                  <div style="max-width:460px;margin:0 auto;background:#fff;border-radius:16px;overflow:hidden;
                              border:1px solid #e8ebf0;box-shadow:0 10px 30px -16px rgba(31,41,80,.18)">
                    <div style="padding:20px 28px;border-bottom:1px solid #e8ebf0;display:flex;align-items:center;gap:11px">
                      <div style="width:34px;height:34px;border-radius:10px;display:flex;align-items:center;
                                  justify-content:center;background:linear-gradient(135deg,#1d4ed8,#4338ca)">
                        <span style="color:#fff;font-weight:800;font-size:13px">SGS</span>
                      </div>
                      <div>
                        <div style="font-size:13px;font-weight:800;color:#0f172a;letter-spacing:.13em">SGS</div>
                        <div style="font-size:10.5px;color:#94a3b8;font-weight:500;margin-top:1px">Sistema de Gestão de Segurança</div>
                      </div>
                    </div>
                    <div style="padding:28px 28px 26px">
                      <div style="font-size:10.5px;font-weight:800;letter-spacing:.16em;text-transform:uppercase;
                                  color:#4338ca;margin-bottom:11px">Segurança da conta</div>
                      <h1 style="margin:0;font-size:22px;font-weight:800;color:#0f172a;
                                 letter-spacing:-.02em;line-height:1.2">Redefinição de senha</h1>
                      <p style="font-size:14px;color:#475569;line-height:1.6;margin:14px 0 0">
                        Recebemos um pedido para redefinir a senha da sua conta SGS.<br/>
                        Use o código abaixo — ele é pessoal e intransferível.
                      </p>
                      <div style="margin-top:22px;background:#f5f6fb;border:1px solid #e8ebf0;
                                  border-radius:14px;padding:20px 18px;text-align:center">
                        <div style="font-size:11px;font-weight:700;letter-spacing:.1em;text-transform:uppercase;
                                    color:#94a3b8;margin-bottom:14px">Seu código de verificação</div>
                        <div style="display:flex;gap:8px;justify-content:center">%s</div>
                        <div style="font-size:12px;color:#b45309;margin-top:15px;font-weight:600">
                          &#9200; Este código expira em 15 minutos
                        </div>
                      </div>
                      <div style="display:flex;gap:10px;align-items:flex-start;margin-top:22px;
                                  padding:13px 14px;background:#f5f6fb;border-radius:11px;border:1px solid #e8ebf0">
                        <div style="font-size:12px;color:#475569;line-height:1.55">
                          Se você <strong style="color:#0f172a">não solicitou</strong> esta alteração,
                          ignore este e-mail. Sua senha permanece a mesma.
                        </div>
                      </div>
                    </div>
                    <div style="padding:18px 28px 22px;border-top:1px solid #e8ebf0;background:#fbfcfe">
                      <div style="font-size:11.5px;color:#94a3b8;line-height:1.6">
                        <strong style="color:#475569">ERS Engenharia de Segurança</strong><br/>
                        Av. Leopoldino de Oliveira, 3000 · Uberaba, MG
                      </div>
                      <div style="font-size:10.5px;color:#b9c0cc;margin-top:12px">
                        Este é um e-mail automático — não responda.
                      </div>
                    </div>
                  </div>
                </body></html>
                """, blocks.toString());

        brevoEmailService.send(to, "Seu código de redefinição de senha — SGS", html);
        log.info("Email de reset enviado para {}", to);
    }
}
