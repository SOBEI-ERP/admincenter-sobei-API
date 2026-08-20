package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.model.entity.InscricaoCongresso;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
@Slf4j
public class CertificadoCongressoService {

    private static final String TEMPLATE_PDF_PATH = "templates/template-certificado.pdf";

    // Cor de fundo creme do certificado (#FFF4BC), extraída do template original
    private static final Color COR_FUNDO = new Color(255, 244, 188);

    // Coordenadas dos placeholders XXX no sistema de coordenadas da página (283x201 pts)
    // Obtidas via análise do content stream com pypdf visitor_text
    private static final float NOME_X = 60.34f;
    private static final float NOME_Y = 106.94f;     // baseline do texto (y a partir do fundo da página)
    private static final float NOME_LARGURA = 116.0f; // de x=60.34 até x=176.47 (início de ", CPF Nº")
    private static final float CPF_X = 201.71f;
    private static final float CPF_Y = 106.94f;
    private static final float CPF_LARGURA = 47.5f;   // de x=201.71 até x=249.26 (início de ",")

    // Tamanho da fonte no espaço da página (7.333 * escala cm 0.75 = 5.5)
    private static final float FONT_SIZE = 5.5f;

    // Altura da cobertura (suficiente para cobrir ascendentes e descendentes)
    private static final float COVER_HEIGHT = 7.0f;
    // Offset abaixo da baseline para cobrir descendentes
    private static final float COVER_DESCENT = 1.5f;

    /**
     * Gera o certificado do participante em formato PDF,
     * usando o template oficial como base e substituindo os placeholders XXX.
     *
     * @param inscricao Dados da inscrição do participante
     * @return byte[] contendo o arquivo PDF gerado
     */
    public byte[] gerarCertificadoPdf(InscricaoCongresso inscricao) {
        try {
            // 1. Carregar o PDF template original
            ClassPathResource templateResource = new ClassPathResource(TEMPLATE_PDF_PATH);
            PdfReader reader;
            try (InputStream is = templateResource.getInputStream()) {
                reader = new PdfReader(is);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfStamper stamper = new PdfStamper(reader, out);

            // 2. Obter a camada de sobreposição da página 1
            PdfContentByte over = stamper.getOverContent(1);

            // 3. Preparar dados do participante
            String nome = (inscricao.getNomeCompleto() != null && !inscricao.getNomeCompleto().isBlank())
                    ? inscricao.getNomeCompleto().toUpperCase().trim()
                    : "PARTICIPANTE";
            String cpfFormatado = formatarCpf(inscricao.getCpf());

            // 4. Fonte para sobreposição (Bold para nome/CPF, mesma cor preta do original)
            BaseFont bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            Font fontBold = new Font(bfBold, FONT_SIZE, Font.BOLD, Color.BLACK);

            // 5. Cobrir o placeholder do NOME com retângulo na cor de fundo
            over.saveState();
            over.setColorFill(COR_FUNDO);
            over.rectangle(NOME_X - 0.5f, NOME_Y - COVER_DESCENT, NOME_LARGURA + 1.0f, COVER_HEIGHT);
            over.fill();
            over.restoreState();

            // 6. Escrever o nome do participante centralizado na área do placeholder
            float nomeCenterX = NOME_X + (NOME_LARGURA / 2.0f);
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER,
                    new Phrase(nome, fontBold), nomeCenterX, NOME_Y, 0);

            // 7. Cobrir o placeholder do CPF com retângulo na cor de fundo
            over.saveState();
            over.setColorFill(COR_FUNDO);
            over.rectangle(CPF_X - 0.5f, CPF_Y - COVER_DESCENT, CPF_LARGURA + 1.0f, COVER_HEIGHT);
            over.fill();
            over.restoreState();

            // 8. Escrever o CPF centralizado na área do placeholder
            float cpfCenterX = CPF_X + (CPF_LARGURA / 2.0f);
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER,
                    new Phrase(cpfFormatado, fontBold), cpfCenterX, CPF_Y, 0);

            // 9. Fechar e retornar
            stamper.close();
            reader.close();

            return out.toByteArray();
        } catch (Exception e) {
            log.error("Erro ao gerar certificado PDF para inscrito ID {}: {}", inscricao.getId(), e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar certificado em PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Formata o CPF no padrão XXX.XXX.XXX-XX caso esteja apenas com números.
     */
    private String formatarCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return "—";
        }
        String digits = cpf.replaceAll("\\D", "");
        if (digits.length() == 11) {
            return String.format("%s.%s.%s-%s",
                    digits.substring(0, 3),
                    digits.substring(3, 6),
                    digits.substring(6, 9),
                    digits.substring(9, 11));
        }
        return cpf;
    }
}
