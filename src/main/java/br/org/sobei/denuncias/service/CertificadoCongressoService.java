package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.model.entity.InscricaoCongresso;
import com.lowagie.text.*;
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

    private static final String BG_TEMPLATE_PATH = "templates/template-certificado-bg.jpg";

    /**
     * Gera o certificado do participante em formato PDF (A4 Paisagem).
     *
     * @param inscricao Dados da inscrição do participante
     * @return byte[] contendo o arquivo PDF gerado
     */
    public byte[] gerarCertificadoPdf(InscricaoCongresso inscricao) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // A4 Paisagem: 842.0f x 595.0f
            Rectangle pageSize = PageSize.A4.rotate();
            Document document = new Document(pageSize, 36, 36, 36, 36);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // 1. Imagem de Fundo (Template Oficial)
            try {
                ClassPathResource bgResource = new ClassPathResource(BG_TEMPLATE_PATH);
                if (bgResource.exists()) {
                    try (InputStream is = bgResource.getInputStream()) {
                        byte[] bgBytes = is.readAllBytes();
                        Image bgImage = Image.getInstance(bgBytes);
                        bgImage.scaleAbsolute(pageSize.getWidth(), pageSize.getHeight());
                        bgImage.setAbsolutePosition(0, 0);

                        PdfContentByte under = writer.getDirectContentUnder();
                        under.addImage(bgImage);
                    }
                } else {
                    log.warn("Imagem de template de certificado não encontrada em: {}", BG_TEMPLATE_PATH);
                }
            } catch (Exception e) {
                log.error("Erro ao carregar imagem de fundo do certificado: {}", e.getMessage(), e);
            }

            // 2. Tipografia e Cores
            Color corPrimaria = new Color(10, 25, 63);       // Azul Marinho SOBEI (#0A193F)
            Color corDourada = new Color(182, 132, 37);      // Dourado SOBEI (#B68425)
            Color corTexto = new Color(55, 65, 81);          // Cinza Escuro (#374151)

            Font fontIntro = FontFactory.getFont(FontFactory.HELVETICA, 13.5f, Font.NORMAL, corTexto);
            Font fontNome = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17f, Font.BOLD, corPrimaria);
            Font fontCpf = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14f, Font.BOLD, corPrimaria);
            Font fontCorpo = FontFactory.getFont(FontFactory.HELVETICA, 13.5f, Font.NORMAL, corTexto);
            Font fontCongresso = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 19f, Font.BOLD, corDourada);
            Font fontTema = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 14f, Font.ITALIC, corPrimaria);
            Font fontCarga = FontFactory.getFont(FontFactory.HELVETICA, 12.5f, Font.NORMAL, corTexto);
            Font fontData = FontFactory.getFont(FontFactory.HELVETICA, 12f, Font.NORMAL, corTexto);
            Font fontAssinaturaNome = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13f, Font.BOLD, corPrimaria);
            Font fontAssinaturaCargo = FontFactory.getFont(FontFactory.HELVETICA, 11f, Font.NORMAL, corTexto);

            // 3. Montagem do Conteúdo com Posicionamento Centralizado
            PdfContentByte over = writer.getDirectContent();

            // Bloco de Texto Principal
            ColumnText ct = new ColumnText(over);
            // Área de texto central: x=60 a 782 (largura 722), y=150 a 430
            ct.setSimpleColumn(60, 140, 782, 435);
            ct.setAlignment(Element.ALIGN_CENTER);

            // Linha 1: A SOBEI...
            Paragraph p1 = new Paragraph();
            p1.setAlignment(Element.ALIGN_CENTER);
            p1.setSpacingBefore(0);
            p1.setSpacingAfter(8);
            p1.add(new Chunk("A SOBEI - Sociedade Beneficente Equilíbrio de Interlagos,", fontIntro));
            ct.addElement(p1);

            // Linha 2: confere a [NOME COMPLETO], CPF Nº [CPF],
            String nome = (inscricao.getNomeCompleto() != null && !inscricao.getNomeCompleto().isBlank())
                    ? inscricao.getNomeCompleto().toUpperCase().trim()
                    : "PARTICIPANTE";
            String cpfFormatado = formatarCpf(inscricao.getCpf());

            Paragraph p2 = new Paragraph();
            p2.setAlignment(Element.ALIGN_CENTER);
            p2.setSpacingAfter(8);
            p2.add(new Chunk("confere a ", fontCorpo));
            p2.add(new Chunk(nome, fontNome));
            p2.add(new Chunk(",  CPF Nº ", fontCorpo));
            p2.add(new Chunk(cpfFormatado, fontCpf));
            p2.add(new Chunk(",", fontCorpo));
            ct.addElement(p2);

            // Linha 3: o presente certificado pela participação no
            Paragraph p3 = new Paragraph();
            p3.setAlignment(Element.ALIGN_CENTER);
            p3.setSpacingAfter(10);
            p3.add(new Chunk("o presente certificado pela participação no", fontCorpo));
            ct.addElement(p3);

            // Linha 4: XX Congresso de Educação Infantil SOBEI 2026
            Paragraph p4 = new Paragraph();
            p4.setAlignment(Element.ALIGN_CENTER);
            p4.setSpacingAfter(6);
            p4.add(new Chunk("XX Congresso de Educação Infantil SOBEI 2026", fontCongresso));
            ct.addElement(p4);

            // Linha 5: Tema
            Paragraph p5 = new Paragraph();
            p5.setAlignment(Element.ALIGN_CENTER);
            p5.setSpacingAfter(12);
            p5.add(new Chunk("“Cuidar, acolher e incluir. Construindo vínculos na primeiríssima infância”", fontTema));
            ct.addElement(p5);

            // Linha 6: Carga Horária e Data
            Paragraph p6 = new Paragraph();
            p6.setAlignment(Element.ALIGN_CENTER);
            p6.setSpacingAfter(6);
            p6.add(new Chunk("Realizado nos dias 11 e 12 de setembro de 2026, com carga horária de 14 (quatorze) horas.", fontCarga));
            ct.addElement(p6);

            // Linha 7: Local e Data de Emissão
            Paragraph p7 = new Paragraph();
            p7.setAlignment(Element.ALIGN_CENTER);
            p7.setSpacingAfter(0);
            p7.add(new Chunk("São Paulo, 12 de setembro de 2026.", fontData));
            ct.addElement(p7);

            ct.go();

            // 4. Bloco de Assinatura (Canto Inferior Esquerdo/Central)
            ColumnText ctAssinatura = new ColumnText(over);
            ctAssinatura.setSimpleColumn(100, 30, 380, 115);
            ctAssinatura.setAlignment(Element.ALIGN_CENTER);

            Paragraph pAssNome = new Paragraph();
            pAssNome.setAlignment(Element.ALIGN_CENTER);
            pAssNome.add(new Chunk("Satie Kochiko", fontAssinaturaNome));
            ctAssinatura.addElement(pAssNome);

            Paragraph pAssCargo = new Paragraph();
            pAssCargo.setAlignment(Element.ALIGN_CENTER);
            pAssCargo.add(new Chunk("Coordenadora Pedagógica", fontAssinaturaCargo));
            ctAssinatura.addElement(pAssCargo);

            ctAssinatura.go();

            document.close();
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
