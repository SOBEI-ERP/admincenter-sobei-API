package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.model.entity.InscricaoCongresso;
import com.lowagie.text.Chunk;
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

    private static final String ASSINATURA_SATIE_PATH = "templates/assinatura-satie.png";
    private static final String ASSINATURA_BALDO_PATH = "templates/assinatura-baldo.png";

    /**
     * Gera o certificado do participante em formato PDF,
     * limpando o bloco de texto e assinaturas antigas do template
     * e redesenhando todo o conteúdo com perfeito alinhamento,
     * hierarquia visual e separação total sem nenhuma sobreposição.
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

            // 4. Cobrir toda a área de texto e assinaturas antigas com a cor de fundo oficial (#FFF4BC)
            over.saveState();
            over.setColorFill(COR_FUNDO);
            // Cobre de x=0 até x=283.5, y=0 até y=122 (toda a área abaixo do banner superior)
            over.rectangle(0f, 0f, 283.5f, 122f);
            over.fill();
            over.restoreState();

            // 5. Configurar Fontes
            BaseFont bfRegular = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont bfItalic = BaseFont.createFont(BaseFont.HELVETICA_OBLIQUE, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

            Font fontRegIntro = new Font(bfRegular, 5.6f, Font.NORMAL, Color.BLACK);
            Font fontBoldIntro = new Font(bfBold, 5.6f, Font.BOLD, Color.BLACK);
            Font fontTitle = new Font(bfRegular, 7.8f, Font.NORMAL, Color.BLACK);
            Font fontTheme = new Font(bfItalic, 6.6f, Font.ITALIC, new Color(30, 30, 30));
            Font fontFooterReg = new Font(bfRegular, 5.4f, Font.NORMAL, new Color(40, 40, 40));
            Font fontDate = new Font(bfRegular, 5.2f, Font.NORMAL, new Color(60, 60, 60));
            Font fontSigName = new Font(bfRegular, 5.2f, Font.NORMAL, Color.BLACK);
            Font fontSigTitle = new Font(bfRegular, 4.6f, Font.NORMAL, new Color(50, 50, 50));

            final float centerX = 141.5f; // Centro horizontal da página (283 / 2)

            // 6. Linha 1: "A SOBEI - Sociedade Beneficente Equilíbrio de Interlagos,"
            Phrase pLinha1 = new Phrase("A SOBEI - Sociedade Beneficente Equilíbrio de Interlagos,", fontRegIntro);
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER, pLinha1, centerX, 112.5f, 0);

            // 7. Linha 2: "confere a [NOME], CPF Nº [CPF]," (Nome e CPF em negrito, 'confere a' na mesma linha)
            float nomeFontSize = calcularFontSizeNome(bfBold, nome, 150f, 5.6f, 4.2f);
            Font fontNome = new Font(bfBold, nomeFontSize, Font.BOLD, Color.BLACK);
            Phrase pLinha2 = new Phrase();
            pLinha2.add(new Chunk("confere a ", fontRegIntro));
            pLinha2.add(new Chunk(nome, fontNome));
            pLinha2.add(new Chunk(", CPF Nº ", fontRegIntro));
            pLinha2.add(new Chunk(cpfFormatado, fontBoldIntro));
            pLinha2.add(new Chunk(",", fontRegIntro));
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER, pLinha2, centerX, 104.5f, 0);

            // 8. Linha 3: "o presente certificado pela participação no"
            Phrase pLinha3 = new Phrase("o presente certificado pela participação no", fontRegIntro);
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER, pLinha3, centerX, 96.5f, 0);

            // 9. Linha 4: Nome do Congresso
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER,
                    new Phrase("XX Congresso de Educação Infantil SOBEI 2026", fontTitle), centerX, 84.0f, 0);

            // 10. Linha 5: Tema do Congresso
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER,
                    new Phrase("“Cuidar, acolher e incluir. Construindo vínculos na primeiríssima infância”", fontTheme), centerX, 73.5f, 0);

            // 11. Linha 6: Carga Horária e Dias
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER,
                    new Phrase("Realizado nos dias 11 e 12/09/2026, com carga horária de 14 (quatorze) horas.", fontFooterReg), centerX, 58.5f, 0);

            // 12. Linha 7: Data de Emissão / Local
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER,
                    new Phrase("São Paulo, 12 de setembro de 2026.", fontDate), centerX, 49.5f, 0);

            // 13. Assinatura Satie Kochiko (Esquerda)
            try {
                ClassPathResource satieRes = new ClassPathResource(ASSINATURA_SATIE_PATH);
                try (InputStream is = satieRes.getInputStream()) {
                    com.lowagie.text.Image imgSatie = com.lowagie.text.Image.getInstance(is.readAllBytes());
                    imgSatie.scaleAbsolute(32.0f, 15.7f);
                    imgSatie.setAbsolutePosition(59.0f, 24.5f);
                    over.addImage(imgSatie);
                }
            } catch (Exception ex) {
                log.warn("Não foi possível carregar assinatura de Satie Kochiko: {}", ex.getMessage());
            }
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER,
                    new Phrase("Satie Kochiko", fontSigName), 75.0f, 17.0f, 0);
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER,
                    new Phrase("Coordenadora", fontSigTitle), 75.0f, 11.0f, 0);

            // 14. Assinatura Luiz Baldo Sobrinho (Direita)
            try {
                ClassPathResource baldoRes = new ClassPathResource(ASSINATURA_BALDO_PATH);
                try (InputStream is = baldoRes.getInputStream()) {
                    com.lowagie.text.Image imgBaldo = com.lowagie.text.Image.getInstance(is.readAllBytes());
                    imgBaldo.scaleAbsolute(31.0f, 20.7f);
                    imgBaldo.setAbsolutePosition(192.5f, 23.5f);
                    over.addImage(imgBaldo);
                }
            } catch (Exception ex) {
                log.warn("Não foi possível carregar assinatura de Luiz Baldo Sobrinho: {}", ex.getMessage());
            }
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER,
                    new Phrase("Luiz Baldo Sobrinho", fontSigName), 208.0f, 17.0f, 0);
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER,
                    new Phrase("Presidente da SOBEI", fontSigTitle), 208.0f, 11.0f, 0);

            // 15. Fechar e retornar
            stamper.close();
            reader.close();

            return out.toByteArray();
        } catch (Exception e) {
            log.error("Erro ao gerar certificado PDF para inscrito ID {}: {}", inscricao.getId(), e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar certificado em PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Calcula o tamanho ideal da fonte para o nome caber na largura disponível sem truncar.
     */
    private float calcularFontSizeNome(BaseFont bf, String text, float maxLargura, float maxFontSize, float minFontSize) {
        for (float size = maxFontSize; size >= minFontSize; size -= 0.2f) {
            float width = bf.getWidthPoint(text, size);
            if (width <= maxLargura) {
                return size;
            }
        }
        return minFontSize;
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
