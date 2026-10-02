package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.model.entity.InscricaoCongresso;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private JavaMailSender mailSender;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private InscricaoCongresso inscricao;
    private byte[] pdfBytes;

    @BeforeEach
    void setUp() {
        inscricao = InscricaoCongresso.builder()
                .id(10)
                .nomeCompleto("Maria da Silva")
                .cpf("123.456.789-00")
                .email("maria.silva@exemplo.com")
                .tipoOsc("SOBEI")
                .unidade("CEI MATRIZ")
                .build();

        pdfBytes = "%PDF-1.4 Mock PDF Content".getBytes();

        ReflectionTestUtils.setField(emailService, "mailEnabled", true);
        ReflectionTestUtils.setField(emailService, "resendApiKey", "");
        ReflectionTestUtils.setField(emailService, "resendFrom", "Congresso SOBEI 2026 <onboarding@resend.dev>");
        ReflectionTestUtils.setField(emailService, "resendApiUrl", "https://api.resend.com/emails");
        ReflectionTestUtils.setField(emailService, "mailFrom", "congresso@sobei.org.br");
        ReflectionTestUtils.setField(emailService, "mailFromName", "Congresso SOBEI 2026");
        ReflectionTestUtils.setField(emailService, "objectMapper", objectMapper);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando o participante não possui e-mail cadastrado")
    void deveLancarExcecaoSemEmail() {
        inscricao.setEmail(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                emailService.enviarCertificadoCongresso(inscricao, pdfBytes)
        );

        assertEquals("O participante não possui endereço de e-mail cadastrado.", ex.getMessage());

        inscricao.setEmail("   ");
        assertThrows(IllegalArgumentException.class, () ->
                emailService.enviarCertificadoCongresso(inscricao, pdfBytes)
        );
    }

    @Test
    @DisplayName("Deve retornar true e simular envio quando mailEnabled for falso")
    void deveSimularEnvioQuandoMailDisabled() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", false);

        boolean resultado = emailService.enviarCertificadoCongresso(inscricao, pdfBytes);

        assertTrue(resultado);
    }

    @Test
    @DisplayName("Deve retornar true no modo de simulação quando não houver chave Resend nem MailSender")
    void deveSimularEnvioSemProvedorAtivo() {
        ReflectionTestUtils.setField(emailService, "mailSender", null);
        ReflectionTestUtils.setField(emailService, "resendApiKey", "");

        boolean resultado = emailService.enviarCertificadoCongresso(inscricao, pdfBytes);

        assertTrue(resultado);
    }

    @Test
    @DisplayName("Deve disparar erro descritivo caso o envio via Resend API falhe com URL ou chave inválida")
    void deveTratarErroAoFalharResend() {
        ReflectionTestUtils.setField(emailService, "resendApiKey", "re_test_dummy_key");
        // Aponta para uma URL inválida para testar tratamento de exceção
        ReflectionTestUtils.setField(emailService, "resendApiUrl", "http://localhost:59999/invalid-endpoint");

        Throwable ex = assertThrows(Throwable.class, () ->
                emailService.enviarCertificadoCongresso(inscricao, pdfBytes)
        );

        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().toLowerCase().contains("resend"), "A mensagem de erro deve referenciar o serviço Resend");
    }
}
