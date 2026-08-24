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

    /**
     * Gera o certificado do participante em formato PDF,
     * limpando o bloco de texto antigo do template e redesenhando
     * todas as linhas de forma perfeitamente padronizada, uniforme e alinhada.
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

            // 4. Cobrir toda a área de texto anterior com a cor de fundo oficial (#FFF4BC)
            over.saveState();
            over.setColorFill(COR_FUNDO);
            // Cobre de x=15 até x=268, y=41 até y=120 (todo o miolo de texto variável)
            over.rectangle(15f, 41f, 253f, 79f);
            over.fill();
            over.restoreState();

            // 5. Configurar Fontes
            BaseFont bfRegular = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont bfItalic = BaseFont.createFont(BaseFont.HELVETICA_OBLIQUE, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont bfBoldItalic = BaseFont.createFont(BaseFont.HELVETICA_BOLDOBLIQUE, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

            Font fontReg55 = new Font(bfRegular, 5.5f, Font.NORMAL, Color.BLACK);
            Font fontBold55 = new Font(bfBold, 5.5f, Font.BOLD, Color.BLACK);
            Font fontTitle = new Font(bfBold, 7.0f, Font.BOLD, Color.BLACK);
            Font fontTheme = new Font(bfBoldItalic, 6.5f, Font.BOLD | Font.ITALIC, new Color(30, 30, 30));
            Font fontFooterReg = new Font(bfRegular, 5.2f, Font.NORMAL, new Color(40, 40, 40));
            Font fontDate = new Font(bfRegular, 5.0f, Font.NORMAL, new Color(60, 60, 60));

            final float centerX = 141.5f; // Centro horizontal da página (283 / 2)

            // 6. Linha 1: "A SOBEI - Sociedade Beneficente Equilíbrio de Interlagos, confere a"
            Phrase pLinha1 = new Phrase();
            pLinha1.add(new Chunk("A ", fontReg55));
            pLinha1.add(new Chunk("SOBEI - Sociedade Beneficente Equilíbrio de Interlagos", fontBold55));
            pLinha1.add(new Chunk(", confere a", fontReg55));
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER, pLinha1, centerX, 114.5f, 0);

            // 7. Linha 2: Nome do Participante em Destaque (com ajuste dinâmico de fonte para caber perfeitamente)
            float nomeFontSize = calcularFontSizeNome(bfBold, nome, 240f, 6.8f, 4.8f);
            Font fontNome = new Font(bfBold, nomeFontSize, Font.BOLD, Color.BLACK);
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER, new Phrase(nome, fontNome), centerX, 106.2f, 0);

            // 8. Linha 3: "CPF Nº XXX.XXX.XXX-XX, o presente certificado pela participação no"
            Phrase pLinha3 = new Phrase();
            pLinha3.add(new Chunk("CPF Nº ", fontReg55));
            pLinha3.add(new Chunk(cpfFormatado, fontBold55));
            pLinha3.add(new Chunk(", o presente certificado pela participação no", fontReg55));
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER, pLinha3, centerX, 97.8f, 0);

            // 9. Linha 4: Nome do Congresso em Negrito
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER,
                    new Phrase("XX Congresso de Educação Infantil SOBEI 2026", fontTitle), centerX, 86.5f, 0);

            // 10. Linha 5: Tema do Congresso
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER,
                    new Phrase("“Cuidar, acolher e incluir. Construindo vínculos na primeiríssima infância”", fontTheme), centerX, 76.5f, 0);

            // 11. Linha 6: Carga Horária e Dias
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER,
                    new Phrase("Realizado nos dias 11 e 12/09/2026, com carga horária de 14 (quatorze) horas.", fontFooterReg), centerX, 58.5f, 0);

            // 12. Linha 7: Data de Emissão / Local
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER,
                    new Phrase("São Paulo, 12 de setembro de 2026.", fontDate), centerX, 48.0f, 0);

            // 13. Fechar e retornar
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
