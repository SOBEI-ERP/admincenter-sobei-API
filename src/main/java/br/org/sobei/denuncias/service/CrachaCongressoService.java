package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.model.entity.InscricaoCongresso;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@Slf4j
public class CrachaCongressoService {

    // Cores oficiais extraídas diretamente do modelo oficial crachas_padronizados.pdf
    private static final Color COR_AZUL_CABECALHO = new Color(46, 116, 181); // #2E74B5 (.1804 .4549 .7098)
    private static final Color COR_TEXTO_PRETO = new Color(0, 0, 0);         // #000000
    private static final Color COR_PONTILHADO_CORTE = new Color(180, 180, 180); // Cinza suave para guia de recorte

    // Dimensões do crachá individual: formato retangular horizontal padrão (9.8cm x 5.6cm)
    private static final float SINGLE_WIDTH = 280f;
    private static final float SINGLE_HEIGHT = 160f;

    // Dimensões da folha A4 (595.28 x 841.89 pt) e grade de 14 etiquetas Tilibra TB182 (2 colunas x 7 linhas - 101.6 x 33.9 mm)
    private static final float PAGE_WIDTH = 595.28f;
    private static final float PAGE_HEIGHT = 841.89f;
    private static final int COLS = 2;
    private static final int ROWS = 7;
    private static final int CRACHAS_POR_PAGINA = COLS * ROWS; // 14 etiquetas por folha

    // Gabarito oficial Tilibra TB182: 101,6 mm de largura x 33,9 mm de altura (288.0 pt x 96.1 pt)
    private static final float SHEET_CARD_WIDTH = 288.0f;  // 101.6 mm
    private static final float SHEET_CARD_HEIGHT = 96.1f;  // 33.9 mm
    private static final float MARGIN_X = (PAGE_WIDTH - (COLS * SHEET_CARD_WIDTH)) / 2.0f;   // 9.64 pt (~3.4 mm de margem lateral)
    private static final float MARGIN_TOP = 60.25f; // ~25.85 mm (deslocado 4 mm para cima a pedido do usuário)
    private static final float GAP_X = 0f;
    private static final float GAP_Y = 0f;

    /**
     * Gera o PDF de um único crachá individual em formato retangular padrão idêntico ao modelo oficial.
     */
    public byte[] gerarCrachaIndividual(InscricaoCongresso inscricao) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Rectangle pageSize = new Rectangle(SINGLE_WIDTH, SINGLE_HEIGHT);
            Document document = new Document(pageSize, 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            PdfContentByte cb = writer.getDirectContent();
            BaseFont bfRegular = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

            desenharCrachaIndividual(cb, bfRegular, bfBold, inscricao, SINGLE_WIDTH, SINGLE_HEIGHT);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Erro ao gerar crachá individual para inscrito ID {}: {}", inscricao.getId(), e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar crachá individual em PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Gera uma ou mais páginas com a grade 3x4 de crachás retangulares padronizados pronta para impressão.
     */
    public byte[] gerarGradeCrachas(List<InscricaoCongresso> inscricoes) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Rectangle pageSize = new Rectangle(PAGE_WIDTH, PAGE_HEIGHT);
            Document document = new Document(pageSize, 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            PdfContentByte cb = writer.getDirectContent();
            BaseFont bfRegular = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

            int total = inscricoes.size();
            for (int i = 0; i < total; i++) {
                int indexOnPage = i % CRACHAS_POR_PAGINA;
                if (i > 0 && indexOnPage == 0) {
                    document.newPage();
                }

                int col = indexOnPage % COLS;
                int row = indexOnPage / COLS; // 0 = topo, 3 = base

                float x = MARGIN_X + (col * SHEET_CARD_WIDTH);
                float y = PAGE_HEIGHT - MARGIN_TOP - ((row + 1) * SHEET_CARD_HEIGHT) - (row * GAP_Y);

                InscricaoCongresso inscricao = inscricoes.get(i);
                desenharCrachaGrade(cb, bfRegular, bfBold, inscricao, x, y, SHEET_CARD_WIDTH, SHEET_CARD_HEIGHT);
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Erro ao gerar folha de crachás em lote: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar grade de crachás em PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Renderiza o crachá individual em formato retangular perfeito (280 x 160 pt) seguindo o modelo oficial.
     */
    private void desenharCrachaIndividual(PdfContentByte cb, BaseFont bfRegular, BaseFont bfBold,
                                          InscricaoCongresso inscricao, float w, float h) {

        // 1. Determinar texto da Unidade / OSC
        String unidadeTexto = obterTextoUnidade(inscricao);

        // 2. Cabeçalho da Unidade (Azul #2E74B5, centralizado)
        float headerY = h - 25f;
        cb.saveState();
        cb.setColorFill(COR_AZUL_CABECALHO);
        cb.setFontAndSize(bfBold, 12.5f);
        float unidadeTextWidth = bfBold.getWidthPoint(unidadeTexto, 12.5f);
        float unidadeX = (w - unidadeTextWidth) / 2.0f;
        cb.beginText();
        cb.setTextMatrix(unidadeX, headerY);
        cb.showText(unidadeTexto);
        cb.endText();

        // Linha azul contínua abaixo do cabeçalho
        float lineY = headerY - 5f;
        float lineWidth = Math.min(w - 40f, 220f);
        float lineX1 = (w - lineWidth) / 2.0f;
        float lineX2 = lineX1 + lineWidth;
        cb.setColorStroke(COR_AZUL_CABECALHO);
        cb.setLineWidth(1.4f);
        cb.setLineDash(0);
        cb.moveTo(lineX1, lineY);
        cb.lineTo(lineX2, lineY);
        cb.stroke();
        cb.restoreState();

        // 3. Nome Completo do Participante (Centro do crachá retangular em negrito)
        String nome = (inscricao.getNomeCompleto() != null && !inscricao.getNomeCompleto().isBlank())
                ? formatarNome(inscricao.getNomeCompleto())
                : "Participante";

        float nomeFontSize = 17.0f;
        if (nome.length() > 25) {
            nomeFontSize = 14.0f;
        }
        if (nome.length() > 36) {
            nomeFontSize = 12.0f;
        }

        Font fontNome = new Font(bfBold, nomeFontSize, Font.BOLD, COR_TEXTO_PRETO);
        Paragraph pNome = new Paragraph(nome, fontNome);
        pNome.setAlignment(Element.ALIGN_CENTER);
        pNome.setLeading(nomeFontSize * 1.15f);

        ColumnText ctNome = new ColumnText(cb);
        ctNome.setSimpleColumn(
                14f,
                38f,
                w - 14f,
                lineY - 8f
        );
        ctNome.addElement(pNome);
        try {
            ctNome.go();
        } catch (Exception e) {
            log.warn("Erro ao renderizar nome no crachá individual: {}", e.getMessage());
        }

        // 4. Oficina na parte inferior (linha única)
        String oficinaTexto = obterTextoOficina(inscricao);

        float paddingLeft = 18f;
        float oficinaY = 18f;
        float oficinaFontSize = 11.0f;
        float totalWidth = w - paddingLeft - 14f;

        desenharLinhaOficina(cb, bfRegular, bfBold, oficinaTexto,
                paddingLeft, oficinaY, totalWidth, oficinaFontSize, 8.0f);
    }

    /**
     * Renderiza um crachá dentro da folha de impressão em grade de 14 etiquetas Tilibra (288 x 108 pt).
     */
    private void desenharCrachaGrade(PdfContentByte cb, BaseFont bfRegular, BaseFont bfBold,
                                     InscricaoCongresso inscricao, float x, float y,
                                     float w, float h) {

        // 0. Guia pontilhada suave para recorte/destaque na folha de etiquetas
        cb.saveState();
        cb.setColorStroke(COR_PONTILHADO_CORTE);
        cb.setLineWidth(0.5f);
        cb.setLineDash(3.0f, 3.0f);
        cb.rectangle(x, y, w, h);
        cb.stroke();
        cb.restoreState();

        // 1. Determinar texto da Unidade / OSC
        String unidadeTexto = obterTextoUnidade(inscricao);

        // 2. Cabeçalho da Unidade (Azul #2E74B5, centralizado)
        float headerY = y + h - 14.5f;
        cb.saveState();
        cb.setColorFill(COR_AZUL_CABECALHO);
        cb.setFontAndSize(bfBold, 9.5f);
        float unidadeTextWidth = bfBold.getWidthPoint(unidadeTexto, 9.5f);
        float unidadeX = x + (w - unidadeTextWidth) / 2.0f;
        cb.beginText();
        cb.setTextMatrix(unidadeX, headerY);
        cb.showText(unidadeTexto);
        cb.endText();

        // Linha azul contínua abaixo do cabeçalho
        float lineY = headerY - 3.0f;
        float lineWidth = Math.min(w - 30f, 180f);
        float lineX1 = x + (w - lineWidth) / 2.0f;
        float lineX2 = lineX1 + lineWidth;
        cb.setColorStroke(COR_AZUL_CABECALHO);
        cb.setLineWidth(1.1f);
        cb.setLineDash(0);
        cb.moveTo(lineX1, lineY);
        cb.lineTo(lineX2, lineY);
        cb.stroke();
        cb.restoreState();

        // 3. Nome Completo do Participante (Centro da etiqueta, perfeitamente equilibrado)
        String nome = (inscricao.getNomeCompleto() != null && !inscricao.getNomeCompleto().isBlank())
                ? formatarNome(inscricao.getNomeCompleto())
                : "Participante";

        float nomeFontSize = 13.0f;
        if (nome.length() > 25) {
            nomeFontSize = 11.0f;
        }
        if (nome.length() > 36) {
            nomeFontSize = 9.5f;
        }

        Font fontNome = new Font(bfBold, nomeFontSize, Font.BOLD, COR_TEXTO_PRETO);
        Paragraph pNome = new Paragraph(nome, fontNome);
        pNome.setAlignment(Element.ALIGN_CENTER);
        pNome.setLeading(nomeFontSize * 1.15f);

        ColumnText ctNome = new ColumnText(cb);
        ctNome.setSimpleColumn(
                x + 10f,
                y + 17.5f,
                x + w - 10f,
                lineY - 2.5f
        );
        ctNome.addElement(pNome);
        try {
            ctNome.go();
        } catch (Exception e) {
            log.warn("Erro ao renderizar nome na grade de crachás: {}", e.getMessage());
        }

        // 4. Oficina na parte inferior da etiqueta (linha única)
        String oficinaTexto = obterTextoOficina(inscricao);

        float paddingLeft = 14f;
        float oficinaY = y + 7.5f;
        float sheetOficinaFontSize = 8.5f;
        float sheetTotalWidth = w - (2 * paddingLeft);

        desenharLinhaOficina(cb, bfRegular, bfBold, oficinaTexto,
                x + paddingLeft, oficinaY, sheetTotalWidth, sheetOficinaFontSize, 6.5f);
    }

    /**
     * Renderiza uma única linha de oficina ("OFICINA: ...") com rótulo em negrito
     * e texto ajustado proporcionalmente para não cortar.
     */
    private void desenharLinhaOficina(PdfContentByte cb, BaseFont bfRegular, BaseFont bfBold,
                                      String oficina, float x, float y,
                                      float totalWidth, float baseFontSize, float minFontSize) {

        float labelWidth = bfBold.getWidthPoint("OFICINA: ", baseFontSize);
        float availableTextWidth = totalWidth - labelWidth;

        // Renderizar rótulo OFICINA:
        cb.saveState();
        cb.setColorFill(COR_TEXTO_PRETO);
        cb.setFontAndSize(bfBold, baseFontSize);
        cb.beginText();
        cb.setTextMatrix(x, y);
        cb.showText("OFICINA: ");
        cb.endText();

        // Renderizar nome da oficina
        float oficinaFontSize = calcularFontSize(bfRegular, oficina, availableTextWidth, baseFontSize, minFontSize);
        String oficinaTextoFormatado = obterTextoAjustado(bfRegular, oficina, availableTextWidth, oficinaFontSize);

        cb.setFontAndSize(bfRegular, oficinaFontSize);
        cb.beginText();
        cb.setTextMatrix(x + labelWidth, y);
        cb.showText(oficinaTextoFormatado);
        cb.endText();
        cb.restoreState();
    }

    /**
     * Determina o texto unificado da oficina para exibição no crachá.
     */
    private String obterTextoOficina(InscricaoCongresso inscricao) {
        String manha = (inscricao.getOficinaManha() != null && !inscricao.getOficinaManha().isBlank())
                ? inscricao.getOficinaManha().trim() : "";
        String tarde = (inscricao.getOficinaTarde() != null && !inscricao.getOficinaTarde().isBlank())
                ? inscricao.getOficinaTarde().trim() : "";

        if (!manha.isBlank() && !tarde.isBlank()) {
            if (manha.equalsIgnoreCase(tarde)) {
                return manha;
            }
            return manha + " / " + tarde;
        } else if (!manha.isBlank()) {
            return manha;
        } else if (!tarde.isBlank()) {
            return tarde;
        }
        return "A Definir";
    }

    private float calcularFontSize(BaseFont bf, String texto, float maxWidth, float baseFontSize, float minFontSize) {
        if (texto == null || texto.isBlank()) return baseFontSize;
        float textWidth = bf.getWidthPoint(texto, baseFontSize);
        if (textWidth <= maxWidth || maxWidth <= 0) {
            return baseFontSize;
        }
        float scaled = baseFontSize * (maxWidth / textWidth);
        return Math.max(minFontSize, scaled);
    }

    private String obterTextoAjustado(BaseFont bf, String texto, float maxWidth, float fontSize) {
        if (texto == null || texto.isBlank()) return "";
        float textWidth = bf.getWidthPoint(texto, fontSize);
        if (textWidth <= maxWidth) {
            return texto;
        }
        String reticencias = "...";
        float retWidth = bf.getWidthPoint(reticencias, fontSize);
        if (retWidth >= maxWidth) return "";

        float targetWidth = maxWidth - retWidth;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (bf.getWidthPoint(sb.toString() + c, fontSize) > targetWidth) {
                break;
            }
            sb.append(c);
        }
        return sb.toString().trim() + reticencias;
    }

    private String obterTextoUnidade(InscricaoCongresso inscricao) {
        if ("SOBEI".equalsIgnoreCase(inscricao.getTipoOsc())) {
            String u = inscricao.getUnidade() != null ? inscricao.getUnidade().trim().toUpperCase() : "UNIDADE SOBEI";
            if (!u.startsWith("CEI ") && !u.startsWith("CEDESP") && !u.startsWith("CCINTER") && !u.startsWith("NCI") && !u.startsWith("TELECENTRO")) {
                return "CEI " + u;
            } else {
                return u;
            }
        } else if (inscricao.getOutraOsc() != null && !inscricao.getOutraOsc().isBlank()) {
            return inscricao.getOutraOsc().trim().toUpperCase();
        } else {
            return "OSC ou Unidade";
        }
    }

    private String formatarNome(String nome) {
        if (nome == null) return "";
        String[] partes = nome.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : partes) {
            if (p.equalsIgnoreCase("de") || p.equalsIgnoreCase("da") || p.equalsIgnoreCase("do") ||
                p.equalsIgnoreCase("das") || p.equalsIgnoreCase("dos") || p.equalsIgnoreCase("e")) {
                sb.append(p.toLowerCase()).append(" ");
            } else if (p.length() > 0) {
                sb.append(Character.toUpperCase(p.charAt(0)))
                  .append(p.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }
}
