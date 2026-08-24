package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.model.entity.InscricaoCongresso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CrachaCongressoServiceTest {

    private CrachaCongressoService crachaService;

    @BeforeEach
    void setUp() {
        crachaService = new CrachaCongressoService();
    }

    @Test
    void testGerarCrachaIndividualComSucesso() {
        InscricaoCongresso inscricao = InscricaoCongresso.builder()
                .id(1)
                .nomeCompleto("Fabiana Cristina Marco Dutra")
                .cpf("123.456.789-00")
                .email("fabiana@sobei.org.br")
                .tipoOsc("SOBEI")
                .unidade("Montanaro")
                .oficinaManha("Fernanda Polezein")
                .oficinaTarde("Fernanda Polezein")
                .build();

        byte[] pdfBytes = crachaService.gerarCrachaIndividual(inscricao);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // Header PDF magic bytes: %PDF-
        assertEquals('%', (char) pdfBytes[0]);
        assertEquals('P', (char) pdfBytes[1]);
        assertEquals('D', (char) pdfBytes[2]);
        assertEquals('F', (char) pdfBytes[3]);
    }

    @Test
    void testGerarGradeCrachasLoteComSucesso() {
        List<InscricaoCongresso> lista = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            lista.add(InscricaoCongresso.builder()
                    .id(i)
                    .nomeCompleto("Participante " + i + " da Silva")
                    .cpf("111.222.333-44")
                    .email("participante" + i + "@sobei.org.br")
                    .tipoOsc(i % 2 == 0 ? "SOBEI" : "OUTRA")
                    .unidade(i % 2 == 0 ? "Montanaro" : null)
                    .outraOsc(i % 2 != 0 ? "Instituto Criar" : null)
                    .oficinaManha("Angelina")
                    .oficinaTarde("Fabiola")
                    .build());
        }

        byte[] pdfBytes = crachaService.gerarGradeCrachas(lista);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        assertEquals('%', (char) pdfBytes[0]);
        assertEquals('P', (char) pdfBytes[1]);
        assertEquals('D', (char) pdfBytes[2]);
        assertEquals('F', (char) pdfBytes[3]);
    }
}
