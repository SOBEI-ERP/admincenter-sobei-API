package br.org.sobei.denuncias.config;

import java.text.Normalizer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Matriz de cotas máximas de participantes por unidade escolar nas oficinas do Congresso SOBEI 2026.
 * Fonte: _PESSOAS POR OFICINA - CONGRESSO 2026.xlsx
 */
public final class OficinaCotasConfig {

    private static final Map<String, Map<String, Integer>> COTAS = new HashMap<>();

    static {
        // 1. Cleide Derenzi Valadas
        registrar("Cleide Derenzi Valadas",
                "Quais os saberes e fazeres tão específicos para, de verdade, atendermos às necessidades dos nossos bebês e crianças pequenas no cotidiano das instituições?",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 1), Map.entry("IMBUIAS", 1), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 1), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 3), Map.entry("CEDRO", 1),
                        Map.entry("OLIVEIRAS", 1), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 1), Map.entry("ARAUCARIAS", 1),
                        Map.entry("IPES", 1)
                ));

        // 2. Rodrigo Cândido
        registrar("Rodrigo Cândido",
                "Quem dança seus males espanta!",
                Map.ofEntries(
                        Map.entry("MONTANARO", 8), Map.entry("LEBLON", 3), Map.entry("IMBUIAS", 4), Map.entry("BELA_VISTA", 5),
                        Map.entry("SABIAS", 3), Map.entry("ACACIAS", 5), Map.entry("ORQUIDEAS", 8), Map.entry("CEDRO", 4),
                        Map.entry("OLIVEIRAS", 3), Map.entry("MACAUBA", 5), Map.entry("CEREJEIRAS", 3), Map.entry("ARAUCARIAS", 3),
                        Map.entry("IPES", 5)
                ));

        // 3. Cristiano dos Santos Araujo
        registrar("Cristiano dos Santos Araujo",
                "Entre contos, brincadeiras e canções",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 1), Map.entry("IMBUIAS", 1), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 1), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 3), Map.entry("CEDRO", 1),
                        Map.entry("OLIVEIRAS", 1), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 1), Map.entry("ARAUCARIAS", 1),
                        Map.entry("IPES", 1)
                ));

        // 4. Maria Cecília Martin Ferri
        registrar("Maria Cecília Martin Ferri",
                "Valorizando diferentes culturas através da arte narrativa.",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 1), Map.entry("IMBUIAS", 1), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 1), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 3), Map.entry("CEDRO", 1),
                        Map.entry("OLIVEIRAS", 1), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 1), Map.entry("ARAUCARIAS", 1),
                        Map.entry("IPES", 1)
                ));

        // 5. Ana Gilda Leocadio
        registrar("Ana Gilda Leocadio",
                "Contando Histórias Para Criar Memórias",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 2), Map.entry("IMBUIAS", 2), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 2), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 3), Map.entry("CEDRO", 2),
                        Map.entry("OLIVEIRAS", 2), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 2), Map.entry("ARAUCARIAS", 2),
                        Map.entry("IPES", 2)
                ));

        // 6. Jaqueline Gomes Silva Veleda
        registrar("Jaqueline Gomes Silva Veleda",
                "Inclusão na Primeira Infância, Além do Diagnóstico",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 1), Map.entry("IMBUIAS", 1), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 1), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 3), Map.entry("CEDRO", 1),
                        Map.entry("OLIVEIRAS", 1), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 1), Map.entry("ARAUCARIAS", 1),
                        Map.entry("IPES", 1)
                ));

        // 7. Márcia Curti de Mello
        registrar("Márcia Curti de Mello",
                "Inclusão no lúdico, como o brincar pode ajudar a superar barreiras",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 1), Map.entry("IMBUIAS", 1), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 1), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 3), Map.entry("CEDRO", 1),
                        Map.entry("OLIVEIRAS", 1), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 1), Map.entry("ARAUCARIAS", 1),
                        Map.entry("IPES", 1)
                ));

        // 8. Leila Saita
        registrar("Leila Saita",
                "Vivências para refletir sobre cuidados corporais de qualidade na creche inspirados na Abordagem Pikler.",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 1), Map.entry("IMBUIAS", 1), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 1), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 3), Map.entry("CEDRO", 1),
                        Map.entry("OLIVEIRAS", 1), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 1), Map.entry("ARAUCARIAS", 1),
                        Map.entry("IPES", 1)
                ));

        // 9. Erika Aparecida da Silva
        registrar("Erika Aparecida da Silva",
                "Brincar, Criar e Pertencer: experiências antirracistas por meio das múltiplas linguagens da infância",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 2), Map.entry("IMBUIAS", 2), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 2), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 3), Map.entry("CEDRO", 2),
                        Map.entry("OLIVEIRAS", 2), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 2), Map.entry("ARAUCARIAS", 2),
                        Map.entry("IPES", 2)
                ));

        // 10. Regiane Lays Jacinto de Brito
        registrar("Regiane Lays Jacinto de Brito",
                "Saberes que alimentam: cuidado, memória e pertencimento na experiência de quem atua na cozinha",
                Map.ofEntries(
                        Map.entry("MONTANARO", 7), Map.entry("LEBLON", 4), Map.entry("IMBUIAS", 5), Map.entry("BELA_VISTA", 6),
                        Map.entry("SABIAS", 4), Map.entry("ACACIAS", 6), Map.entry("ORQUIDEAS", 9), Map.entry("CEDRO", 5),
                        Map.entry("OLIVEIRAS", 4), Map.entry("MACAUBA", 6), Map.entry("CEREJEIRAS", 4), Map.entry("ARAUCARIAS", 4),
                        Map.entry("IPES", 6)
                ));

        // 11. Liliane Laviano
        registrar("Liliane Laviano",
                "Jogo da Arquitetura Cerebral — Como as experiências na primeira infância moldam a arquitetura do cérebro.",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 1), Map.entry("IMBUIAS", 1), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 1), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 3), Map.entry("CEDRO", 1),
                        Map.entry("OLIVEIRAS", 1), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 1), Map.entry("ARAUCARIAS", 1),
                        Map.entry("IPES", 1)
                ));

        // 12. Talita Regina Lopes de Oliveira Marques
        registrar("Talita Regina Lopes de Oliveira Marques",
                "A importância do Brincar com Areia na Educação Infantil",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 2), Map.entry("IMBUIAS", 2), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 2), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 3), Map.entry("CEDRO", 2),
                        Map.entry("OLIVEIRAS", 2), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 2), Map.entry("ARAUCARIAS", 2),
                        Map.entry("IPES", 2)
                ));

        // 13. Irene Izilda da Silva
        registrar("Irene Izilda da Silva",
                "A literatura infantil como ferramenta de educação antirracista dialogando com as relações étnico-raciais na pedagogia da infância.",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 2), Map.entry("IMBUIAS", 2), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 2), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 3), Map.entry("CEDRO", 2),
                        Map.entry("OLIVEIRAS", 2), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 2), Map.entry("ARAUCARIAS", 2),
                        Map.entry("IPES", 2)
                ));

        // 14. Patrícia Couto Gimael
        registrar("Patrícia Couto Gimael",
                "Cuidados, linguagem e inclusão",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 1), Map.entry("IMBUIAS", 1), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 1), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 3), Map.entry("CEDRO", 1),
                        Map.entry("OLIVEIRAS", 1), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 1), Map.entry("ARAUCARIAS", 1),
                        Map.entry("IPES", 1)
                ));

        // 15. Raissa Cintra
        registrar("Raissa Cintra",
                "Corpo e Movimento",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 1), Map.entry("IMBUIAS", 1), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 1), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 3), Map.entry("CEDRO", 1),
                        Map.entry("OLIVEIRAS", 1), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 1), Map.entry("ARAUCARIAS", 1),
                        Map.entry("IPES", 1)
                ));

        // 16. Shirley Maria de Oliveira
        registrar("Shirley Maria de Oliveira",
                "Dos acalantos às rodas de verso: a música tradicional da infância embalando os brinquedos de criança. (Shirley Oliveira)",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 2), Map.entry("IMBUIAS", 2), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 2), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 3), Map.entry("CEDRO", 2),
                        Map.entry("OLIVEIRAS", 2), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 2), Map.entry("ARAUCARIAS", 2),
                        Map.entry("IPES", 2)
                ));

        // 17. Elaine Maria da Silva
        registrar("Elaine Maria da Silva",
                "Dos acalantos às rodas de verso: a música tradicional da infância embalando os brinquedos de criança. (Elaine Silva)",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 2), Map.entry("IMBUIAS", 2), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 2), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 3), Map.entry("CEDRO", 2),
                        Map.entry("OLIVEIRAS", 2), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 2), Map.entry("ARAUCARIAS", 2),
                        Map.entry("IPES", 2)
                ));

        // 18. Rose Brito
        registrar("Rose Brito",
                "Entre Cantos, Contos e Batucadas",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 1), Map.entry("IMBUIAS", 1), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 1), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 3), Map.entry("CEDRO", 1),
                        Map.entry("OLIVEIRAS", 1), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 1), Map.entry("ARAUCARIAS", 1),
                        Map.entry("IPES", 1)
                ));

        // 19. Ivani Magalhães
        registrar("Ivani Magalhães",
                "Rodas e brincadeiras cantadas",
                Map.ofEntries(
                        Map.entry("MONTANARO", 1), Map.entry("LEBLON", 1), Map.entry("IMBUIAS", 1), Map.entry("BELA_VISTA", 1),
                        Map.entry("SABIAS", 1), Map.entry("ACACIAS", 1), Map.entry("ORQUIDEAS", 1), Map.entry("CEDRO", 1),
                        Map.entry("OLIVEIRAS", 1), Map.entry("MACAUBA", 1), Map.entry("CEREJEIRAS", 1), Map.entry("ARAUCARIAS", 1),
                        Map.entry("IPES", 1)
                ));

        // 20. Márcia Polacchini
        registrar("Márcia Polacchini",
                "Jogos Teatrais",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 1), Map.entry("IMBUIAS", 1), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 1), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 3), Map.entry("CEDRO", 1),
                        Map.entry("OLIVEIRAS", 1), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 1), Map.entry("ARAUCARIAS", 1),
                        Map.entry("IPES", 1)
                ));

        // 21. Leticia de Almeida Oliveira
        registrar("Leticia de Almeida Oliveira",
                "Alimentação segura e pedagógica na escola: manejo clínico e comportamental (0 a 4 anos)",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 2), Map.entry("IMBUIAS", 2), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 2), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 2), Map.entry("CEDRO", 2),
                        Map.entry("OLIVEIRAS", 2), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 2), Map.entry("ARAUCARIAS", 2),
                        Map.entry("IPES", 2)
                ));

        // 22. Juliana Neves e Leticia Alves
        registrar("Juliana Neves e Leticia Alves",
                "Escuta Ativa: A Fonoaudiologia no Cotidiano da Pedagogia da Infância",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 1), Map.entry("IMBUIAS", 1), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 1), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 3), Map.entry("CEDRO", 1),
                        Map.entry("OLIVEIRAS", 1), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 1), Map.entry("ARAUCARIAS", 1),
                        Map.entry("IPES", 1)
                ));

        // 23. Shirley da Silva
        registrar("Shirley da Silva",
                "Motricidade Livre",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 1), Map.entry("IMBUIAS", 1), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 1), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 3), Map.entry("CEDRO", 1),
                        Map.entry("OLIVEIRAS", 1), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 1), Map.entry("ARAUCARIAS", 1),
                        Map.entry("IPES", 1)
                ));
        registrar("Shirley da Silva Santos",
                "Motrocidade Livre",
                Map.ofEntries(
                        Map.entry("MONTANARO", 2), Map.entry("LEBLON", 1), Map.entry("IMBUIAS", 1), Map.entry("BELA_VISTA", 2),
                        Map.entry("SABIAS", 1), Map.entry("ACACIAS", 2), Map.entry("ORQUIDEAS", 3), Map.entry("CEDRO", 1),
                        Map.entry("OLIVEIRAS", 1), Map.entry("MACAUBA", 2), Map.entry("CEREJEIRAS", 1), Map.entry("ARAUCARIAS", 1),
                        Map.entry("IPES", 1)
                ));
    }

    private static void registrar(String ministrante, String tema, Map<String, Integer> limites) {
        COTAS.put(normalizarTexto(ministrante), limites);
        COTAS.put(normalizarTexto(tema), limites);
    }

    public static int obterCotaUnidade(String oficinaTemaOuMinistrante, String unidade) {
        if (oficinaTemaOuMinistrante == null || unidade == null) return 999;
        String chaveOficina = normalizarTexto(oficinaTemaOuMinistrante);
        String chaveUnidade = normalizarUnidade(unidade);

        // Busca exata
        Map<String, Integer> limites = COTAS.get(chaveOficina);

        // Fallback para correspondência parcial
        if (limites == null) {
            for (Map.Entry<String, Map<String, Integer>> entry : COTAS.entrySet()) {
                if (chaveOficina.contains(entry.getKey()) || entry.getKey().contains(chaveOficina)) {
                    limites = entry.getValue();
                    break;
                }
            }
        }

        if (limites == null) return 999;
        return limites.getOrDefault(chaveUnidade, 999);
    }

    public static String normalizarUnidade(String unidade) {
        if (unidade == null) return "";
        String u = unidade.trim().toUpperCase();
        u = u.replaceAll("^(CEI|CEDESP|CCINTER|NCI|TELECENTRO)\\s+", "");
        String limpo = removerAcentos(u);

        if (limpo.contains("MONTANARO")) return "MONTANARO";
        if (limpo.contains("LEBLON")) return "LEBLON";
        if (limpo.contains("IMBUIA")) return "IMBUIAS";
        if (limpo.contains("BELA VISTA") || limpo.contains("BELAVISTA")) return "BELA_VISTA";
        if (limpo.contains("SABIA")) return "SABIAS";
        if (limpo.contains("ACACIA")) return "ACACIAS";
        if (limpo.contains("ORQUIDEA")) return "ORQUIDEAS";
        if (limpo.contains("CEDRO")) return "CEDRO";
        if (limpo.contains("OLIVEIRA")) return "OLIVEIRAS";
        if (limpo.contains("MACAUBA")) return "MACAUBA";
        if (limpo.contains("CEREJEIRA") || limpo.contains("JACOMO")) return "CEREJEIRAS";
        if (limpo.contains("ARAUCARIA")) return "ARAUCARIAS";
        if (limpo.contains("IPE")) return "IPES";

        return limpo.replaceAll("[^A-Z0-9]", "_");
    }

    public static String normalizarTexto(String texto) {
        if (texto == null) return "";
        return removerAcentos(texto.toLowerCase().trim())
                .replaceAll("\\s+", " ")
                .replaceAll("[.,;:!?]+$", "")
                .trim();
    }

    private static String removerAcentos(String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }
}
