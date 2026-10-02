package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.model.entity.InscricaoCongresso;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired(required = false)
    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.resend.api-key:}")
    private String resendApiKey;

    @Value("${app.resend.from:Congresso SOBEI 2026 <onboarding@resend.dev>}")
    private String resendFrom;

    @Value("${app.resend.api-url:https://api.resend.com/emails}")
    private String resendApiUrl;

    @Value("${app.mail.from:congresso@sobei.org.br}")
    private String mailFrom;

    @Value("${app.mail.from-name:Congresso SOBEI 2026}")
    private String mailFromName;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    /**
     * Envia o certificado em anexo para o e-mail do participante com corpo HTML estilizado.
     * Prioriza o envio direto via Resend API quando configurado.
     *
     * @param inscricao Dados do inscrito
     * @param pdfBytes  Conteúdo do arquivo PDF gerado
     * @return true se enviado com sucesso ou simulado em dev
     */
    public boolean enviarCertificadoCongresso(InscricaoCongresso inscricao, byte[] pdfBytes) {
        String emailDestino = inscricao.getEmail();
        String nome = inscricao.getNomeCompleto();

        if (emailDestino == null || emailDestino.isBlank()) {
            log.warn("Tentativa de envio de certificado para inscrito sem e-mail cadastrado (ID: {})", inscricao.getId());
            throw new IllegalArgumentException("O participante não possui endereço de e-mail cadastrado.");
        }

        if (!mailEnabled) {
            log.info("[SIMULAÇÃO DE E-MAIL] Certificado gerado para '{}' <{}>. Disparo de e-mail desabilitado globalmente.", nome, emailDestino);
            return true;
        }

        // 1. Prioridade: Envio via Resend API se a chave estiver configurada
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            return enviarViaResend(inscricao, emailDestino, nome, pdfBytes);
        }

        // 2. Fallback: Envio via Spring Mail (SMTP) se houver servidor configurado
        if (mailSender != null) {
            return enviarViaSmtp(inscricao, emailDestino, nome, pdfBytes);
        }

        // 3. Simulação: Caso nenhum provedor de envio esteja ativo em ambiente dev
        log.info("[SIMULAÇÃO DE E-MAIL] Certificado gerado para '{}' <{}>. Nenhuma chave do Resend ou servidor SMTP configurado.", nome, emailDestino);
        return true;
    }

    /**
     * Envia o e-mail com anexo utilizando a API REST oficial do Resend.
     */
    private boolean enviarViaResend(InscricaoCongresso inscricao, String emailDestino, String nome, byte[] pdfBytes) {
        try {
            String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);
            String corpoHtml = buildCorpoEmailCertificado(nome);
            String nomeArquivo = "Certificado_Congresso_SOBEI_2026.pdf";

            Map<String, Object> attachment = Map.of(
                    "filename", nomeArquivo,
                    "content", base64Pdf
            );

            Map<String, Object> payload = Map.of(
                    "from", resendFrom,
                    "to", List.of(emailDestino),
                    "subject", "Certificado de Participação — XX Congresso de Educação Infantil SOBEI 2026",
                    "html", corpoHtml,
                    "attachments", List.of(attachment)
            );

            String requestBody = objectMapper.writeValueAsString(payload);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(resendApiUrl))
                    .header("Authorization", "Bearer " + resendApiKey.trim())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode >= 200 && statusCode < 300) {
                String idEnvio = "";
                try {
                    JsonNode root = objectMapper.readTree(responseBody);
                    if (root.has("id")) {
                        idEnvio = root.get("id").asText();
                    }
                } catch (Exception ignored) {}

                log.info("Certificado enviado com sucesso via Resend API para '{}' <{}> (Resend ID: {}, Inscrição ID: {})",
                        nome, emailDestino, idEnvio, inscricao.getId());
                return true;
            } else {
                log.error("Falha ao enviar e-mail via Resend para '{}' <{}>. HTTP Status: {} - Body: {}",
                        nome, emailDestino, statusCode, responseBody);
                String errorMsg = "Erro na API Resend (HTTP " + statusCode + "): " + responseBody;
                try {
                    JsonNode root = objectMapper.readTree(responseBody);
                    if (root.has("message")) {
                        String rawMsg = root.get("message").asText();
                        if (statusCode == 403 && rawMsg.contains("only send testing emails to your own email address")) {
                            errorMsg = "O remetente de testes (onboarding@resend.dev) só permite envios para o seu próprio e-mail cadastrado no Resend. Para disparar certificados para outros e-mails (" + emailDestino + "), é necessário cadastrar e verificar seu domínio oficial no painel do Resend (resend.com/domains).";
                        } else {
                            errorMsg = rawMsg;
                        }
                    }
                } catch (Exception ignored) {}
                throw new IllegalArgumentException(errorMsg);
            }
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception e) {
            log.error("Erro durante comunicação com a API do Resend para '{}' <{}>: {}", nome, emailDestino, e.getMessage(), e);
            throw new IllegalArgumentException("Falha ao comunicar com o serviço Resend: " + e.getMessage(), e);
        }
    }

    /**
     * Envia o e-mail com anexo utilizando JavaMailSender (SMTP).
     */
    private boolean enviarViaSmtp(InscricaoCongresso inscricao, String emailDestino, String nome, byte[] pdfBytes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(emailDestino);
            helper.setSubject("Certificado de Participação — XX Congresso de Educação Infantil SOBEI 2026");

            String corpoHtml = buildCorpoEmailCertificado(nome);
            helper.setText(corpoHtml, true);

            String nomeArquivo = "Certificado_Congresso_SOBEI_2026.pdf";
            helper.addAttachment(nomeArquivo, new ByteArrayResource(pdfBytes), "application/pdf");

            mailSender.send(message);
            log.info("Certificado enviado com sucesso por SMTP para '{}' <{}> (Inscrição ID: {})", nome, emailDestino, inscricao.getId());
            return true;
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail por SMTP para '{}' <{}>: {}", nome, emailDestino, e.getMessage(), e);
            throw new RuntimeException("Não foi possível enviar o e-mail via SMTP: " + e.getMessage(), e);
        }
    }

    /**
     * Monta o template HTML responsivo do e-mail com as cores da identidade institucional da SOBEI.
     */
    private String buildCorpoEmailCertificado(String nome) {
        String primeiroNome = (nome != null && !nome.isBlank()) ? nome.split(" ")[0] : "Participante";
        String template = """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
              <meta charset="UTF-8">
              <style>
                body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #FAF5EB; margin: 0; padding: 20px; color: #1F2937; }
                .container { max-width: 600px; margin: 0 auto; background-color: #FFFFFF; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 25px rgba(10, 25, 63, 0.08); border: 1px solid #E5E7EB; }
                .header { background: linear-gradient(135deg, #0A193F 0%, #16285A 100%); padding: 34px 25px; text-align: center; color: #FFFFFF; }
                .badge-table { margin: 0 auto; border: 2.5px solid #FFFFFF; border-collapse: collapse; border-spacing: 0; }
                .badge-xx { background-color: #FFFFFF; color: #0A193F; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; font-size: 17px; font-weight: 900; padding: 5px 12px; line-height: 1; letter-spacing: -0.5px; text-align: center; vertical-align: middle; }
                .badge-congresso { background-color: transparent; color: #FFFFFF; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; font-size: 17px; font-weight: 900; padding: 5px 14px; line-height: 1; letter-spacing: 2px; text-align: center; vertical-align: middle; }
                .badge-sub { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #FFFFFF; font-size: 13px; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase; margin-top: 10px; margin-bottom: 22px; }
                .header-divider { border-top: 1px solid rgba(255, 255, 255, 0.2); padding-top: 18px; margin-top: 4px; }
                .header h1 { margin: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; font-size: 21px; font-weight: 900; letter-spacing: 0.04em; text-transform: uppercase; color: #FFFFFF; }
                .content { padding: 32px 30px; line-height: 1.65; font-size: 15px; color: #374151; }
                .greeting { font-size: 18px; font-weight: bold; color: #0A193F; margin-bottom: 16px; }
                .footer { background-color: #F3F4F6; padding: 24px 30px; text-align: center; font-size: 12px; color: #6B7280; border-top: 1px solid #E5E7EB; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <!-- Badge Oficial: XX CONGRESSO -->
                  <table class="badge-table" align="center" cellpadding="0" cellspacing="0" style="margin: 0 auto; border: 2.5px solid #FFFFFF; border-collapse: collapse; border-spacing: 0;">
                    <tr>
                      <td class="badge-xx" style="background-color: #FFFFFF; color: #0A193F; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; font-size: 17px; font-weight: 900; padding: 5px 12px; line-height: 1; letter-spacing: -0.5px; text-align: center; vertical-align: middle;">XX</td>
                      <td class="badge-congresso" style="background-color: transparent; color: #FFFFFF; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; font-size: 17px; font-weight: 900; padding: 5px 14px; line-height: 1; letter-spacing: 2px; text-align: center; vertical-align: middle;">CONGRESSO</td>
                    </tr>
                  </table>
                  
                  <!-- Subtítulo: DE EDUCAÇÃO INFANTIL SOBEI -->
                  <div class="badge-sub" style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #FFFFFF; font-size: 13px; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase; margin-top: 10px; margin-bottom: 22px;">
                    DE EDUCAÇÃO INFANTIL SOBEI
                  </div>

                  <!-- Título: CERTIFICADO DE PARTICIPAÇÃO -->
                  <div class="header-divider" style="border-top: 1px solid rgba(255, 255, 255, 0.2); padding-top: 18px; margin-top: 4px;">
                    <h1 style="margin: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; font-size: 21px; font-weight: 900; letter-spacing: 0.04em; text-transform: uppercase; color: #FFFFFF;">Certificado de Participação</h1>
                  </div>
                </div>
                <div class="content">
                  <div class="greeting">Olá, {{NOME}}!</div>
                  <p>Agradecemos imensamente a sua valiosa participação no <strong>XX Congresso de Educação Infantil SOBEI 2026</strong>, realizado nos dias 11 e 12 de setembro.</p>
                  <p>É com grande alegria que disponibilizamos o seu <strong>Certificado Oficial de Participação</strong> em anexo neste e-mail.</p>
                  <p>Você pode baixar, imprimir ou salvar o seu certificado para comprovação de participação pedagógica.</p>
                  <p style="margin-top: 28px;">Atenciosamente,<br><strong>Comissão Organizadora do Congresso SOBEI</strong><br><span style="color: #6B7280; font-size: 13px;">Sociedade Beneficente Equilíbrio de Interlagos</span></p>
                </div>
                <div class="footer">
                  <p>SOBEI — Celebrando Histórias, Inspirando Futuros!<br>Este é um e-mail automático gerado pelo Portal do Congresso SOBEI.</p>
                </div>
              </div>
            </body>
            </html>
            """;
        return template.replace("{{NOME}}", primeiroNome);
    }
}
