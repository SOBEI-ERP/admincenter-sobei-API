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
    private static final float MARGIN_TOP = 60.25f; // Margem calibrada no papel
    private static final float GAP_X = 0f;
    private static final float GAP_Y = 0f;

    /**
     * Gera o PDF de um único crachá posicionado exatamente na 1ª etiqueta
     * da folha de etiquetas Tilibra TB182 (folha A4 completa com as demais posições em branco).
     */
    public byte[] gerarCrachaIndividual(InscricaoCongresso inscricao) {
        return gerarGradeCrachas(List.of(inscricao));
    }

    /**
     * Gera uma ou mais páginas com a grade de crachás/etiquetas no padrão Tilibra TB182 pronta para impressão.
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
                int row = indexOnPage / COLS; // 0 = topo, 6 = base

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
     * Renderiza um crachá dentro da folha de impressão em grade de 14 etiquetas Tilibra TB182 (288 x 96.1 pt).
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

        // 1. Marca d'água oficial do Congresso no topo (XX CONGRESSO / DE EDUCAÇÃO INFANTIL SOBEI)
        desenharMarcaDaguaCongresso(cb, bfBold, x, y, w, h);

        // 2. Nome Completo do Participante (Topo / Destaque Principal - Centralizado)
        String nome = (inscricao.getNomeCompleto() != null && !inscricao.getNomeCompleto().isBlank())
                ? formatarNome(inscricao.getNomeCompleto())
                : "Participante";

        float nomeFontSize = 14.0f;
        if (nome.length() > 25) {
            nomeFontSize = 12.0f;
        }
        if (nome.length() > 36) {
            nomeFontSize = 10.5f;
        }

        Font fontNome = new Font(bfBold, nomeFontSize, Font.BOLD, COR_TEXTO_PRETO);
        Paragraph pNome = new Paragraph(nome, fontNome);
        pNome.setAlignment(Element.ALIGN_CENTER);
        pNome.setLeading(nomeFontSize * 1.15f);

        ColumnText ctNome = new ColumnText(cb);
        ctNome.setSimpleColumn(
                x + 8f,
                y + 40.0f,
                x + w - 8f,
                y + h - 16.0f
        );
        ctNome.addElement(pNome);
        try {
            ctNome.go();
        } catch (Exception e) {
            log.warn("Erro ao renderizar nome na grade de crachás: {}", e.getMessage());
        }

        // 3. Unidade / OSC (Abaixo do Nome - Centralizado em Azul Oficial)
        String unidadeTexto = obterTextoUnidade(inscricao);
        float unidadeY = y + 25.5f;
        cb.saveState();
        cb.setColorFill(COR_AZUL_CABECALHO);
        cb.setFontAndSize(bfBold, 9.5f);
        float unidadeTextWidth = bfBold.getWidthPoint(unidadeTexto, 9.5f);
        float unidadeX = x + (w - unidadeTextWidth) / 2.0f;
        cb.beginText();
        cb.setTextMatrix(unidadeX, unidadeY);
        cb.showText(unidadeTexto);
        cb.endText();

        // Linha azul contínua de apoio abaixo da unidade
        float lineY = unidadeY - 3.0f;
        float lineWidth = Math.min(w - 50f, Math.max(unidadeTextWidth + 20f, 130f));
        float lineX1 = x + (w - lineWidth) / 2.0f;
        float lineX2 = lineX1 + lineWidth;
        cb.setColorStroke(COR_AZUL_CABECALHO);
        cb.setLineWidth(1.0f);
        cb.setLineDash(0);
        cb.moveTo(lineX1, lineY);
        cb.lineTo(lineX2, lineY);
        cb.stroke();
        cb.restoreState();

        // 4. OFICINA (Aumentado e 100% Centralizado no Rodapé)
        String oficinaTexto = obterTextoOficina(inscricao);
        float oficinaY = y + 8.5f;
        float sheetOficinaFontSize = 10.5f;
        float sheetTotalWidth = w - 24f;

        desenharLinhaOficinaCentralizada(cb, bfRegular, bfBold, oficinaTexto,
                x, oficinaY, w, sheetTotalWidth, sheetOficinaFontSize, 8.0f);
    }

    /**
     * Desenha a marca d'água oficial estilizada do Congresso no topo da etiqueta.
     */
    private void desenharMarcaDaguaCongresso(PdfContentByte cb, BaseFont bfBold, float x, float y, float w, float h) {
        cb.saveState();
        Color corMarca = new Color(130, 140, 175); // Tom suave elegante de marca d'água
        cb.setColorStroke(corMarca);
        cb.setColorFill(corMarca);

        float topY = y + h - 3.5f;

        // 1. Moldura retangular: "XX | CONGRESSO"
        float boxWidth = 145f;
        float boxHeight = 8.5f;
        float boxX = x + (w - boxWidth) / 2.0f;
        float boxY = topY - boxHeight;

        cb.setLineWidth(0.6f);
        cb.setLineDash(0);
        cb.rectangle(boxX, boxY, boxWidth, boxHeight);
        cb.stroke();

        // Divisória vertical separando o "XX" de "CONGRESSO"
        float xxWidth = 24f;
        cb.moveTo(boxX + xxWidth, boxY);
        cb.lineTo(boxX + xxWidth, boxY + boxHeight);
        cb.stroke();

        // Texto "XX"
        cb.setFontAndSize(bfBold, 7.2f);
        float xxTextWidth = bfBold.getWidthPoint("XX", 7.2f);
        cb.beginText();
        cb.setTextMatrix(boxX + (xxWidth - xxTextWidth) / 2.0f, boxY + 2.0f);
        cb.showText("XX");
        cb.endText();

        // Texto "CONGRESSO"
        cb.setFontAndSize(bfBold, 6.8f);
        float congressoTextWidth = bfBold.getWidthPoint("CONGRESSO", 6.8f);
        float congressoAvailable = boxWidth - xxWidth;
        cb.beginText();
        cb.setTextMatrix(boxX + xxWidth + (congressoAvailable - congressoTextWidth) / 2.0f, boxY + 2.0f);
        cb.showText("CONGRESSO");
        cb.endText();

        // 2. Subtítulo: "DE EDUCAÇÃO INFANTIL SOBEI"
        String subtitulo = "DE EDUCAÇÃO INFANTIL SOBEI";
        float subFontSize = 4.8f;
        cb.setFontAndSize(bfBold, subFontSize);
        float subWidth = bfBold.getWidthPoint(subtitulo, subFontSize);
        cb.beginText();
        cb.setTextMatrix(x + (w - subWidth) / 2.0f, boxY - 4.5f);
        cb.showText(subtitulo);
        cb.endText();

        cb.restoreState();
    }

    /**
     * Renderiza a linha de oficina aumentada e perfeitamente centralizada na horizontal.
     */
    private void desenharLinhaOficinaCentralizada(PdfContentByte cb, BaseFont bfRegular, BaseFont bfBold,
                                                String oficina, float cardX, float y, float cardWidth,
                                                float maxAvailableWidth, float baseFontSize, float minFontSize) {

        float labelWidth = bfBold.getWidthPoint("OFICINA: ", baseFontSize);
        float availableTextWidth = maxAvailableWidth - labelWidth;

        // Calcular tamanho de fonte da oficina e largura ajustada
        float oficinaFontSize = calcularFontSize(bfRegular, oficina, availableTextWidth, baseFontSize, minFontSize);
        String oficinaTextoFormatado = obterTextoAjustado(bfRegular, oficina, availableTextWidth, oficinaFontSize);
        float textoWidth = bfRegular.getWidthPoint(oficinaTextoFormatado, oficinaFontSize);

        float totalLinhaWidth = labelWidth + textoWidth;
        float startX = cardX + (cardWidth - totalLinhaWidth) / 2.0f;

        // Renderizar rótulo OFICINA:
        cb.saveState();
        cb.setColorFill(COR_TEXTO_PRETO);
        cb.setFontAndSize(bfBold, baseFontSize);
        cb.beginText();
        cb.setTextMatrix(startX, y);
        cb.showText("OFICINA: ");
        cb.endText();

        // Renderizar texto da oficina
        cb.setFontAndSize(bfRegular, oficinaFontSize);
        cb.beginText();
        cb.setTextMatrix(startX + labelWidth, y);
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
