package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.config.OficinaCotasConfig;
import br.org.sobei.denuncias.dto.DenunciaStatsDto;
import br.org.sobei.denuncias.dto.response.EstatisticaCongressoResponse;
import br.org.sobei.denuncias.dto.response.EstatisticaResponse;
import br.org.sobei.denuncias.model.entity.Denuncia;
import br.org.sobei.denuncias.model.entity.InscricaoCongresso;
import br.org.sobei.denuncias.model.enums.TipoDenuncia;
import br.org.sobei.denuncias.repository.InscricaoCongressoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstatisticaService {

    private static final Pattern PATTERN_DIACRITICOS = Pattern.compile("\\p{M}");
    private static final Pattern PATTERN_PAREN = Pattern.compile("\\((.*?)\\)");
    private static final Pattern PATTERN_NAO_ALFANUM = Pattern.compile("[^a-z0-9]");
    private static final Pattern PATTERN_SPLIT_OSC = Pattern.compile("\\s+[-/|:]\\s+");
    private static final Pattern PATTERN_SIGLA_MAIUSCULA = Pattern.compile(".*\\b[A-Z]{2,}\\b.*");

    private final EntityManager entityManager;
    private final InscricaoCongressoRepository inscricaoRepository;

    @Transactional(readOnly = true)
    public EstatisticaResponse obterEstatisticas(String tipo, String unidade, String dataInicio, String dataFim) {
        Specification<Denuncia> spec = (root, query, cb) -> cb.conjunction();

        if (StringUtils.hasText(tipo)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tipo"), TipoDenuncia.valueOf(tipo.toUpperCase())));
        }
        if (StringUtils.hasText(unidade)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("unidade"), unidade));
        }
        if (StringUtils.hasText(dataInicio)) {
            LocalDate start = LocalDate.parse(dataInicio);
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataAbertura"), start.atStartOfDay()));
        }
        if (StringUtils.hasText(dataFim)) {
            LocalDate end = LocalDate.parse(dataFim);
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataAbertura"), end.atTime(23, 59, 59)));
        }

        // Utiliza Criteria API para trazer apenas os campos necessários, aplicando a Specification
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DenunciaStatsDto> query = cb.createQuery(DenunciaStatsDto.class);
        Root<Denuncia> root = query.from(Denuncia.class);

        Predicate predicate = spec.toPredicate(root, query, cb);
        if (predicate != null) {
            query.where(predicate);
        }

        query.select(cb.construct(
                DenunciaStatsDto.class,
                root.get("unidade"),
                root.get("tipo"),
                root.get("estado"),
                root.get("prioridade")
        ));

        List<DenunciaStatsDto> todas = entityManager.createQuery(query).getResultList();

        Map<String, Long> porUnidade = todas.stream()
                .filter(d -> d.getUnidade() != null)
                .collect(Collectors.groupingBy(DenunciaStatsDto::getUnidade, Collectors.counting()));

        Map<String, Long> porTipo = todas.stream()
                .collect(Collectors.groupingBy(d -> d.getTipo().name(), Collectors.counting()));

        Map<String, Long> porStatus = todas.stream()
                .collect(Collectors.groupingBy(d -> d.getEstado().name(), Collectors.counting()));

        Map<String, Long> porPrioridade = todas.stream()
                .filter(d -> d.getPrioridade() != null)
                .collect(Collectors.groupingBy(d -> d.getPrioridade().name(), Collectors.counting()));

        var tiposList = porTipo.entrySet().stream()
                .map(e -> EstatisticaResponse.LabelValue.builder().name(e.getKey()).value(e.getValue()).build())
                .collect(Collectors.toList());

        var statusList = porStatus.entrySet().stream()
                .map(e -> EstatisticaResponse.LabelValue.builder().name(e.getKey()).value(e.getValue()).build())
                .collect(Collectors.toList());

        var prioridadesList = porPrioridade.entrySet().stream()
                .map(e -> EstatisticaResponse.LabelValue.builder().name(e.getKey()).value(e.getValue()).build())
                .collect(Collectors.toList());

        return EstatisticaResponse.builder()
                .porUnidade(porUnidade)
                .distribuicao(EstatisticaResponse.DistribuicaoStats.builder()
                        .tipos(tiposList)
                        .status(statusList)
                        .prioridades(prioridadesList)
                        .build())
                .build();
    }

    public record OficinaMetadata(
            String id,
            String ministrante,
            String tema,
            String categoria,
            int capacidade,
            String chaveTema,
            String chaveMinistrante
    ) {
        public OficinaMetadata(String id, String ministrante, String tema, String categoria, int capacidade) {
            this(
                    id,
                    ministrante,
                    tema,
                    categoria,
                    capacidade,
                    OficinaCotasConfig.normalizarTexto(tema),
                    OficinaCotasConfig.normalizarTexto(ministrante)
            );
        }
    }

    private static final List<OficinaMetadata> OFICINAS_CATALOGO = List.of(
            new OficinaMetadata("cleide-derenzi", "Cleide Derenzi Valadas", "Quais os saberes e fazeres tão específicos para, de verdade, atendermos às necessidades dos nossos bebês e crianças pequenas no cotidiano das instituições?", "Primeiríssima Infância", 26),
            new OficinaMetadata("rodrigo-candido", "Rodrigo Cândido", "Quem dança seus males espanta!", "Expressão Corporal & Dança", 66),
            new OficinaMetadata("cristiano-santos", "Cristiano dos Santos Araujo", "Entre contos, brincadeiras e canções.", "Música & Tradição Oral", 31),
            new OficinaMetadata("maria-cecilia", "Maria Cecília Martin Ferri", "Valorizando diferentes culturas através da arte narrativa.", "Arte Narrativa & Culturas", 26),
            new OficinaMetadata("ana-gilda", "Ana Gilda Leocadio", "Contando Histórias Para Criar Memórias.", "Contação de Histórias", 36),
            new OficinaMetadata("jaqueline-gomes", "Jaqueline Gomes Silva Veleda", "Inclusão na Primeira Infância, Além do Diagnóstico.", "Educação Inclusiva", 26),
            new OficinaMetadata("marcia-curti", "Márcia Curti de Mello", "Inclusão no lúdico, como o brincar pode ajudar a superar barreiras.", "Lúdico & Acessibilidade", 31),
            new OficinaMetadata("leila-saita", "Leila Saita", "Vivências para refletir sobre cuidados corporais de qualidade na creche inspirados na Abordagem Pikler.", "Abordagem Pikler", 31),
            new OficinaMetadata("erika-silva", "Erika Aparecida da Silva", "Brincar, Criar e Pertencer: experiências antirracistas por meio das múltiplas linguagens da infância.", "Educação Antirracista", 46),
            new OficinaMetadata("regiane-lays", "Regiane Lays Jacinto de Brito", "Saberes que alimentam: cuidado, memória e pertencimento na experiência de quem atua na cozinha.", "Cuidado & Terapia Integrativa", 76),
            new OficinaMetadata("liliane-laviano", "Liliane Laviano", "Jogo da Arquitetura Cerebral — Como as experiências na primeira infância moldam a arquitetura do cérebro.", "Neurociência & Desenvolvimento", 31),
            new OficinaMetadata("talita-marques", "Talita Regina Lopes de Oliveira Marques", "A importância do Brincar com Areia na Educação Infantil.", "Brincar Sensorial", 36),
            new OficinaMetadata("irene-silva", "Irene Izilda da Silva", "A literatura infantil como ferramenta de educação antirracista dialogando com as relações étnico-raciais na pedagogia da infância.", "Literatura & Relações Étnico-Raciais", 36),
            new OficinaMetadata("patricia-couto", "Patrícia Couto Gimael", "Cuidados, linguagem e inclusão.", "Linguagem & Cuidados", 31),
            new OficinaMetadata("raissa-cintra", "Raissa Cintra", "Corpo e Movimento.", "Psicomotricidade & Movimento", 26),
            new OficinaMetadata("shirley-oliveira", "Shirley Maria de Oliveira", "Dos acalantos às rodas de verso: a música tradicional da infância embalando os brinquedos de criança. (Shirley Oliveira)", "Música & Cultura Popular", 36),
            new OficinaMetadata("elaine-silva", "Elaine Maria da Silva", "Dos acalantos às rodas de verso: a música tradicional da infância embalando os brinquedos de criança. (Elaine Silva)", "Música & Cultura Popular", 36),
            new OficinaMetadata("rose-brito", "Rose Brito", "Entre Cantos, Contos e Batucadas.", "Musicalidade & Contos", 26),
            new OficinaMetadata("ivani-magalhaes", "Ivani Magalhães", "Rodas e brincadeiras cantadas.", "Música & Tradição Popular", 26),
            new OficinaMetadata("marcia-polacchini", "Márcia Polacchini", "Jogos Teatrais.", "Teatro & Expressão Artística", 31),
            new OficinaMetadata("leticia-oliveira", "Leticia de Almeida Oliveira", "Alimentação segura e pedagógica na escola: manejo clínico e comportamental (0 a 4 anos).", "Nutrição & Manejo Clínico", 36),
            new OficinaMetadata("juliana-leticia", "Leticia Alves", "Escuta Ativa: A Fonoaudiologia no Cotidiano da Pedagogia da Infância.", "Fonoaudiologia & Escuta Ativa", 26),
            new OficinaMetadata("shirley-silva", "Shirley da Silva", "Motricidade Livre.", "Desenvolvimento Motor & Psicomotricidade", 31)
    );

    @Transactional(readOnly = true)
    public EstatisticaCongressoResponse obterEstatisticasCongresso() {
        List<InscricaoCongresso> todas = inscricaoRepository.findAll();

        long totalInscritos = todas.size();
        long limiteVagas = 900;
        double percentualPreenchimento = totalInscritos > 0
                ? Math.round(((double) totalInscritos / limiteVagas * 100) * 10.0) / 10.0
                : 0.0;

        long totalSobei = 0;
        long totalComOficina = 0;
        long presentesGeral = 0;
        long presentesDia11 = 0;
        long presentesDia12 = 0;
        long presentesAmbosDias = 0;

        Map<String, List<InscricaoCongresso>> porUnidadeMap = new HashMap<>();
        List<InscricaoCongresso> outrasInscricoes = new ArrayList<>();
        Map<LocalDate, List<InscricaoCongresso>> porDataMap = new TreeMap<>();

        int numOficinas = OFICINAS_CATALOGO.size();
        long[] totalPorOficina = new long[numOficinas];
        long[] sobeiPorOficina = new long[numOficinas];

        LocalDate hoje = LocalDate.now();

        // Passagem única sobre todas as inscrições para consolidar métricas em O(N)
        for (InscricaoCongresso i : todas) {
            boolean isSobei = "SOBEI".equalsIgnoreCase(i.getTipoOsc());
            if (isSobei) {
                totalSobei++;
                String nomeUnidade = normalizarRotuloUnidade(i.getUnidade());
                porUnidadeMap.computeIfAbsent(nomeUnidade, k -> new ArrayList<>()).add(i);
            } else {
                outrasInscricoes.add(i);
            }

            String of = i.getOficina() != null && !i.getOficina().isBlank()
                    ? i.getOficina()
                    : (i.getOficinaManha() != null && !i.getOficinaManha().isBlank()
                        ? i.getOficinaManha()
                        : i.getOficinaTarde());

            boolean temOficina = of != null && !of.isBlank();
            if (temOficina) {
                totalComOficina++;
                String chaveOf = OficinaCotasConfig.normalizarTexto(of);
                if (!chaveOf.isEmpty()) {
                    for (int idx = 0; idx < numOficinas; idx++) {
                        if (isChaveOficinaMatch(chaveOf, OFICINAS_CATALOGO.get(idx))) {
                            totalPorOficina[idx]++;
                            if (isSobei) {
                                sobeiPorOficina[idx]++;
                            }
                        }
                    }
                }
            }

            boolean p11 = Boolean.TRUE.equals(i.getPresenteDia11());
            boolean p12 = Boolean.TRUE.equals(i.getPresenteDia12());
            if (Boolean.TRUE.equals(i.getPresente())) presentesGeral++;
            if (p11) presentesDia11++;
            if (p12) presentesDia12++;
            if (p11 && p12) presentesAmbosDias++;

            LocalDate dataInsc = i.getDataInscricao() != null ? i.getDataInscricao().toLocalDate() : hoje;
            porDataMap.computeIfAbsent(dataInsc, k -> new ArrayList<>()).add(i);
        }

        long totalOutrasOsc = totalInscritos - totalSobei;

        double percentualSobei = totalInscritos > 0
                ? Math.round(((double) totalSobei / totalInscritos * 100) * 10.0) / 10.0
                : 0.0;
        double percentualOutrasOsc = totalInscritos > 0
                ? Math.round(((double) totalOutrasOsc / totalInscritos * 100) * 10.0) / 10.0
                : 0.0;

        long totalSemOficina = totalInscritos - totalComOficina;

        double percentualComOficina = totalInscritos > 0
                ? Math.round(((double) totalComOficina / totalInscritos * 100) * 10.0) / 10.0
                : 0.0;
        double percentualSemOficina = totalInscritos > 0
                ? Math.round(((double) totalSemOficina / totalInscritos * 100) * 10.0) / 10.0
                : 0.0;

        // 1. Unidades SOBEI
        final long finalTotalSobei = totalSobei;
        List<EstatisticaCongressoResponse.UnidadeCongressoStat> porUnidade = porUnidadeMap.entrySet().stream()
                .map(e -> {
                    String nomeUnidade = e.getKey();
                    long total = e.getValue().size();
                    long comOf = e.getValue().stream()
                            .filter(i -> (i.getOficina() != null && !i.getOficina().isBlank())
                                    || (i.getOficinaManha() != null && !i.getOficinaManha().isBlank()))
                            .count();
                    long semOf = total - comOf;
                    double part = finalTotalSobei > 0 ? Math.round(((double) total / finalTotalSobei * 100) * 10.0) / 10.0 : 0.0;
                    return EstatisticaCongressoResponse.UnidadeCongressoStat.builder()
                            .unidade(nomeUnidade)
                            .totalInscritos(total)
                            .comOficina(comOf)
                            .semOficina(semOf)
                            .percentualDoTotal(part)
                            .build();
                })
                .sorted(Comparator.comparingLong(EstatisticaCongressoResponse.UnidadeCongressoStat::getTotalInscritos).reversed())
                .collect(Collectors.toList());

        // 2. Oficinas (23 oficinas)
        List<EstatisticaCongressoResponse.OficinaCongressoStat> porOficina = new ArrayList<>(numOficinas);
        for (int idx = 0; idx < numOficinas; idx++) {
            OficinaMetadata ofMeta = OFICINAS_CATALOGO.get(idx);
            long totalOf = totalPorOficina[idx];
            long sobeiOf = sobeiPorOficina[idx];
            long outrasOf = totalOf - sobeiOf;
            int capacidade = ofMeta.capacidade();
            int restantes = Math.max(0, capacidade - (int) totalOf);
            double taxaOcup = Math.round(((double) totalOf / capacidade * 100) * 10.0) / 10.0;
            String status = taxaOcup >= 100.0 ? "ESGOTADA" : (taxaOcup >= 80.0 ? "QUASE_CHEIA" : "DISPONIVEL");

            porOficina.add(EstatisticaCongressoResponse.OficinaCongressoStat.builder()
                    .id(ofMeta.id())
                    .tema(ofMeta.tema())
                    .ministrante(ofMeta.ministrante())
                    .categoria(ofMeta.categoria())
                    .capacidadeSala(capacidade)
                    .totalInscritos(totalOf)
                    .inscritosSobei(sobeiOf)
                    .inscritosOutrasOsc(outrasOf)
                    .vagasRestantes(restantes)
                    .percentualOcupacao(taxaOcup)
                    .status(status)
                    .build());
        }
        porOficina.sort(Comparator.comparingLong(EstatisticaCongressoResponse.OficinaCongressoStat::getTotalInscritos).reversed());

        // 3. Outras OSCs (unificando pequenas variações de nomes como CT-Vidas, CT Vidas, etc.)
        List<EstatisticaCongressoResponse.OutraOscStat> porOutraOsc = agruparOutrasOscs(outrasInscricoes, totalOutrasOsc);

        // 4. Evolução Temporal (Gráfico de Crescimento)
        DateTimeFormatter dtfDiaMes = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter dtfIso = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<EstatisticaCongressoResponse.EvolucaoInscricaoStat> evolucao = new ArrayList<>(porDataMap.size());
        long acumulador = 0;
        for (Map.Entry<LocalDate, List<InscricaoCongresso>> entry : porDataMap.entrySet()) {
            LocalDate d = entry.getKey();
            List<InscricaoCongresso> inscritosNoDia = entry.getValue();
            long noDia = inscritosNoDia.size();
            acumulador += noDia;
            long sobeiDia = inscritosNoDia.stream().filter(i -> "SOBEI".equalsIgnoreCase(i.getTipoOsc())).count();
            long outrasDia = noDia - sobeiDia;

            evolucao.add(EstatisticaCongressoResponse.EvolucaoInscricaoStat.builder()
                    .data(d.format(dtfDiaMes))
                    .dataCompleta(d.format(dtfIso))
                    .noDia(noDia)
                    .acumulado(acumulador)
                    .sobeiNoDia(sobeiDia)
                    .outrasOscNoDia(outrasDia)
                    .build());
        }

        return EstatisticaCongressoResponse.builder()
                .totalInscritos(totalInscritos)
                .limiteVagas(limiteVagas)
                .percentualPreenchimento(percentualPreenchimento)
                .totalSobei(totalSobei)
                .totalOutrasOsc(totalOutrasOsc)
                .percentualSobei(percentualSobei)
                .percentualOutrasOsc(percentualOutrasOsc)
                .totalComOficina(totalComOficina)
                .totalSemOficina(totalSemOficina)
                .percentualComOficina(percentualComOficina)
                .percentualSemOficina(percentualSemOficina)
                .presentesGeral(presentesGeral)
                .presentesDia11(presentesDia11)
                .presentesDia12(presentesDia12)
                .presentesAmbosDias(presentesAmbosDias)
                .porUnidade(porUnidade)
                .porOficina(porOficina)
                .porOutraOsc(porOutraOsc)
                .evolucaoInscricoes(evolucao)
                .build();
    }

    private String normalizarRotuloUnidade(String u) {
        if (u == null || u.isBlank()) return "Não informada";
        String norm = u.trim();
        String upper = norm.toUpperCase();
        if (!upper.startsWith("CEI ") && !upper.startsWith("CEDESP") && !upper.startsWith("CCINTER")
                && !upper.startsWith("NCI") && !upper.startsWith("MATRIZ")) {
            return "CEI " + norm;
        }
        return norm;
    }

    private boolean isChaveOficinaMatch(String chaveInscrito, OficinaMetadata ofMeta) {
        String chaveTema = ofMeta.chaveTema();
        String chaveMin = ofMeta.chaveMinistrante();
        if (chaveInscrito.equals(chaveTema) || chaveInscrito.equals(chaveMin)) return true;
        if (chaveInscrito.contains(chaveTema) || chaveTema.contains(chaveInscrito)) return true;
        return chaveMin.length() >= 8 && (chaveInscrito.contains(chaveMin) || chaveMin.contains(chaveInscrito));
    }

    /**
     * Unifica variações e pequenas diferenças no nome de outras instituições (ex: CT-Vidas, CT Vidas, CTVidas, etc.)
     */
    private List<EstatisticaCongressoResponse.OutraOscStat> agruparOutrasOscs(List<InscricaoCongresso> outrasInscricoes, long totalOutrasOsc) {
        if (outrasInscricoes.isEmpty()) {
            return Collections.emptyList();
        }

        // Agrupa por chave canônica
        Map<String, List<String>> gruposChave = new HashMap<>();
        for (InscricaoCongresso insc : outrasInscricoes) {
            String raw = insc.getOutraOsc();
            if (raw == null || raw.trim().isBlank()) {
                raw = "Outras Instituições";
            } else {
                raw = raw.trim();
            }
            String chave = gerarChaveCanonicaOsc(raw);
            gruposChave.computeIfAbsent(chave, k -> new ArrayList<>()).add(raw);
        }

        List<EstatisticaCongressoResponse.OutraOscStat> resultado = new ArrayList<>(gruposChave.size());
        for (Map.Entry<String, List<String>> entry : gruposChave.entrySet()) {
            List<String> ocorrencias = entry.getValue();
            long total = ocorrencias.size();
            String nomeEscolhido = escolherMelhorNomeOsc(ocorrencias);
            double part = totalOutrasOsc > 0
                    ? Math.round(((double) total / totalOutrasOsc * 100) * 10.0) / 10.0
                    : 0.0;

            resultado.add(EstatisticaCongressoResponse.OutraOscStat.builder()
                    .nomeOsc(nomeEscolhido)
                    .totalInscritos(total)
                    .percentualOutras(part)
                    .build());
        }

        resultado.sort(Comparator.comparingLong(EstatisticaCongressoResponse.OutraOscStat::getTotalInscritos).reversed());
        return resultado;
    }

    private String gerarChaveCanonicaOsc(String raw) {
        if (raw == null || raw.trim().isBlank()) {
            return "outras";
        }
        // 1. Remover acentos
        String semAcento = PATTERN_DIACRITICOS.matcher(Normalizer.normalize(raw.trim(), Normalizer.Form.NFD))
                .replaceAll("")
                .toLowerCase();

        // 2. Se houver sigla/código entre parênteses, ex: 'Centro de Treinamento (CT-Vidas)'
        Matcher parenMatcher = PATTERN_PAREN.matcher(semAcento);
        if (parenMatcher.find()) {
            String inside = PATTERN_NAO_ALFANUM.matcher(parenMatcher.group(1)).replaceAll("");
            if (inside.length() >= 2 && inside.length() <= 12) {
                return inside;
            }
        }

        // 3. Se houver separador de cláusula com espaços, ex: 'CTVidas - Centro de Treinamento das Vidas'
        String[] partes = PATTERN_SPLIT_OSC.split(semAcento);
        String principal = partes[0].trim();

        // 4. Remover pontuação e manter apenas caracteres alfanuméricos
        String alfa = PATTERN_NAO_ALFANUM.matcher(principal).replaceAll("");
        return alfa.isEmpty() ? "outras" : alfa;
    }

    private String escolherMelhorNomeOsc(List<String> nomes) {
        if (nomes == null || nomes.isEmpty()) return "Outras Instituições";

        // Contagem de frequência
        Map<String, Long> contagens = nomes.stream()
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        // Seleciona o melhor nome baseado em pontuação
        return contagens.keySet().stream()
                .max((a, b) -> {
                    long scoreA = contagens.get(a) * 10L;
                    long scoreB = contagens.get(b) * 10L;

                    // Prefere nomes sem separadores longos como " - "
                    if (a.contains(" - ")) scoreA -= 5L;
                    if (b.contains(" - ")) scoreB -= 5L;

                    // Prefere siglas em caixa alta (ex: CT)
                    if (PATTERN_SIGLA_MAIUSCULA.matcher(a).matches()) scoreA += 3L;
                    if (PATTERN_SIGLA_MAIUSCULA.matcher(b).matches()) scoreB += 3L;

                    // Prefere primeira letra maiúscula
                    if (Character.isUpperCase(a.charAt(0))) scoreA += 2L;
                    if (Character.isUpperCase(b.charAt(0))) scoreB += 2L;

                    if (scoreA != scoreB) {
                        return Long.compare(scoreA, scoreB);
                    }
                    // Em caso de empate, tamanho mais conciso
                    return Integer.compare(b.length(), a.length());
                })
                .orElse(nomes.get(0));
    }
}
