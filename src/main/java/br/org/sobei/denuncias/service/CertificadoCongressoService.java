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

    // Tamanho base da fonte no espaço da página (7.333 * escala cm 0.75 = 5.5)
    private static final float FONT_SIZE = 5.5f;

    private static final String FONT_MEDIUM_PATH = "fonts/Montserrat-Medium.ttf";
    private static final String FONT_SEMIBOLD_PATH = "fonts/Montserrat-SemiBold.ttf";

    /**
     * Gera o certificado do participante em formato PDF,
     * preservando a arte, tipografia, assinaturas e legendas originais do documento oficial,
     * cobrindo e preenchendo com precisão apenas a linha de atribuição (Nome e CPF).
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

            // 4. Cobrir apenas a Linha 2 ("confere a [NOME], CPF Nº [CPF],") com a cor de fundo oficial (#FFF4BC)
            // Coordenadas iText (origem inferior-esquerda, página 283 x 201):
            // y_bottom = 105.1f, y_top = 112.9f (altura 7.8f), x = 20.0f a 263.0f (largura 243.0f)
            over.saveState();
            over.setColorFill(COR_FUNDO);
            over.rectangle(20.0f, 105.1f, 243.0f, 7.8f);
            over.fill();
            over.restoreState();

            // 5. Configurar Fontes Montserrat (Medium para o texto padrão e SemiBold para o negrito suave)
            BaseFont bfMedium = loadBaseFont(FONT_MEDIUM_PATH, BaseFont.HELVETICA);
            BaseFont bfSemiBold = loadBaseFont(FONT_SEMIBOLD_PATH, BaseFont.HELVETICA_BOLD);

            float fontSize = FONT_SIZE; // 5.5f (tamanho padrão do documento original)
            final float maxLarguraLinha = 235.0f;

            // Calcular a largura total da linha 2 com o tamanho base
            float wPrefix = bfMedium.getWidthPoint("confere a ", fontSize);
            float wNome = bfSemiBold.getWidthPoint(nome, fontSize);
            float wCpfPrefix = bfMedium.getWidthPoint(",  CPF Nº ", fontSize);
            float wCpf = bfSemiBold.getWidthPoint(cpfFormatado, fontSize);
            float wSuffix = bfMedium.getWidthPoint(",", fontSize);
            float larguraTotal = wPrefix + wNome + wCpfPrefix + wCpf + wSuffix;

            // Se o nome for muito longo, ajusta a escala proporcionalmente para caber com folga
            if (larguraTotal > maxLarguraLinha) {
                fontSize = Math.max(3.8f, fontSize * (maxLarguraLinha / larguraTotal));
            }

            Font fontReg = new Font(bfMedium, fontSize, Font.NORMAL, Color.BLACK);
            Font fontBold = new Font(bfSemiBold, fontSize, Font.NORMAL, Color.BLACK);

            // 6. Montar Linha 2 centralizada
            Phrase pLinha2 = new Phrase();
            pLinha2.add(new Chunk("confere a ", fontReg));
            pLinha2.add(new Chunk(nome, fontBold));
            pLinha2.add(new Chunk(",  CPF Nº ", fontReg));
            pLinha2.add(new Chunk(cpfFormatado, fontBold));
            pLinha2.add(new Chunk(",", fontReg));

            final float centerX = 141.5f; // Centro da página (283 / 2)
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER, pLinha2, centerX, 107.0f, 0);

            // 7. Fechar e retornar
            stamper.close();
            reader.close();

            return out.toByteArray();
        } catch (Exception e) {
            log.error("Erro ao gerar certificado PDF para inscrito ID {}: {}", inscricao.getId(), e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar certificado em PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Carrega fonte TrueType a partir dos recursos ou aplica fallback para fonte padrão.
     */
    private BaseFont loadBaseFont(String resourcePath, String fallbackStandardFont) {
        try {
            ClassPathResource res = new ClassPathResource(resourcePath);
            if (res.exists()) {
                try (InputStream is = res.getInputStream()) {
                    byte[] fontBytes = is.readAllBytes();
                    return BaseFont.createFont(resourcePath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
                }
            }
        } catch (Exception e) {
            log.warn("Não foi possível carregar fonte customizada {}: {}. Usando fallback {}.", resourcePath, e.getMessage(), fallbackStandardFont);
        }
        try {
            return BaseFont.createFont(fallbackStandardFont, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar fonte fallback: " + e.getMessage(), e);
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
