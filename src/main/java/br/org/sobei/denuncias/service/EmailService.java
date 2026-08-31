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
                        errorMsg = root.get("message").asText();
                    }
                } catch (Exception ignored) {}
                throw new RuntimeException("Falha ao enviar certificado via Resend: " + errorMsg);
            }
        } catch (Exception e) {
            log.error("Erro durante comunicação com a API do Resend para '{}' <{}>: {}", nome, emailDestino, e.getMessage(), e);
            if (e instanceof RuntimeException re && re.getMessage() != null && re.getMessage().contains("Resend")) {
                throw re;
            }
            throw new RuntimeException("Falha ao enviar e-mail via Resend: " + e.getMessage(), e);
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
        return """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
              <meta charset="UTF-8">
              <style>
                body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #FAF5EB; margin: 0; padding: 20px; color: #1F2937; }
                .container { max-width: 600px; margin: 0 auto; background-color: #FFFFFF; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 25px rgba(10, 25, 63, 0.08); border: 1px solid #E5E7EB; }
                .header { background: linear-gradient(135deg, #0A193F 0%, #16285A 100%); padding: 36px 30px; text-align: center; color: #FFFFFF; }
                .header h1 { margin: 0; font-size: 22px; font-weight: 800; letter-spacing: 0.03em; text-transform: uppercase; }
                .header p { margin: 8px 0 0; font-size: 14px; color: #FFC400; font-weight: 600; }
                .content { padding: 32px 30px; line-height: 1.65; font-size: 15px; color: #374151; }
                .greeting { font-size: 18px; font-weight: bold; color: #0A193F; margin-bottom: 16px; }
                .card-info { background-color: #F8FAFC; border-left: 4px solid #D49B2A; padding: 16px 20px; border-radius: 8px; margin: 24px 0; }
                .card-info p { margin: 4px 0; font-size: 14px; }
                .footer { background-color: #F3F4F6; padding: 24px 30px; text-align: center; font-size: 12px; color: #6B7280; border-top: 1px solid #E5E7EB; }
                .badge { display: inline-block; background-color: #FAF5EB; color: #B68425; font-weight: bold; font-size: 12px; padding: 4px 12px; border-radius: 20px; border: 1px solid #D49B2A; margin-bottom: 12px; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <div class="badge">CONGRESSO SOBEI 2026</div>
                  <h1>Certificado de Participação</h1>
                  <p>XX Congresso de Educação Infantil SOBEI</p>
                </div>
                <div class="content">
                  <div class="greeting">Olá, %s!</div>
                  <p>Agradecemos imensamente a sua valiosa participação no <strong>XX Congresso de Educação Infantil SOBEI 2026</strong>, realizado nos dias 11 e 12 de setembro.</p>
                  <p>É com grande alegria que disponibilizamos o seu <strong>Certificado Oficial de Participação</strong> em anexo neste e-mail.</p>
                  
                  <div class="card-info">
                    <p><strong>Evento:</strong> XX Congresso de Educação Infantil SOBEI 2026</p>
                    <p><strong>Tema:</strong> <em>Cuidar, acolher e incluir. Construindo vínculos na primeiríssima infância</em></p>
                    <p><strong>Carga Horária:</strong> 14 (quatorze) horas</p>
                    <p><strong>Data:</strong> 11 e 12 de Setembro de 2026</p>
                  </div>
                  
                  <p>Você pode baixar, imprimir ou salvar o seu certificado para comprovação de participação pedagógica.</p>
                  <p style="margin-top: 28px;">Atenciosamente,<br><strong>Comissão Organizadora do Congresso SOBEI</strong><br><span style="color: #6B7280; font-size: 13px;">Sociedade Beneficente Equilíbrio de Interlagos</span></p>
                </div>
                <div class="footer">
                  <p>SOBEI — Celebrando Histórias, Inspirando Futuros!<br>Este é um e-mail automático gerado pelo Portal do Congresso SOBEI.</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(primeiroNome);
    }
}
