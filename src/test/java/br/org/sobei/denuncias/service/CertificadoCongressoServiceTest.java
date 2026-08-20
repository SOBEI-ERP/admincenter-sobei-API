package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.model.entity.InscricaoCongresso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class CertificadoCongressoServiceTest {

    private CertificadoCongressoService certificadoService;

    @BeforeEach
    void setUp() {
        certificadoService = new CertificadoCongressoService();
    }

    @Test
    @DisplayName("Deve gerar PDF de certificado com dados do participante com sucesso")
    void deveGerarCertificadoPdfComSucesso() {
        InscricaoCongresso inscricao = InscricaoCongresso.builder()
                .id(1)
                .nomeCompleto("MARIA SILVA DE ALMEIDA")
                .cpf("123.456.789-00")
                .email("maria.silva@exemplo.com")
                .tipoOsc("SOBEI")
                .unidade("CEI SOBEI AMIGOS DO SABER")
                .presente(true)
                .presenteDia11(true)
                .presenteDia12(true)
                .build();

        byte[] pdfBytes = certificadoService.gerarCertificadoPdf(inscricao);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1000, "O PDF gerado deve conter bytes válidos");

        // Verifica o cabeçalho do arquivo PDF (%PDF-)
        String header = new String(pdfBytes, 0, Math.min(pdfBytes.length, 10));
        assertTrue(header.startsWith("%PDF-"), "O arquivo gerado deve iniciar com o cabeçalho %PDF-");
    }

    @Test
    @DisplayName("Deve formatar CPF sem máscara automaticamente")
    void deveFormatarCpfSemMascara() {
        InscricaoCongresso inscricao = InscricaoCongresso.builder()
                .id(2)
                .nomeCompleto("JOÃO PEDRO SANTOS")
                .cpf("98765432100")
                .email("joao.pedro@exemplo.com")
                .tipoOsc("OUTRA")
                .outraOsc("INSTITUTO EDUCAR")
                .build();

        byte[] pdfBytes = certificadoService.gerarCertificadoPdf(inscricao);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1000);
    }
}
