package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.DenunciaStatsDto;
import br.org.sobei.denuncias.dto.response.EstatisticaResponse;
import br.org.sobei.denuncias.model.entity.Denuncia;
import br.org.sobei.denuncias.model.enums.TipoDenuncia;
import br.org.sobei.denuncias.config.OficinaCotasConfig;
import br.org.sobei.denuncias.dto.response.EstatisticaCongressoResponse;
import br.org.sobei.denuncias.model.entity.InscricaoCongresso;
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

    public record OficinaMetadata(String id, String ministrante, String tema, String categoria, int capacidade) {}

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

        long totalSobei = todas.stream()
                .filter(i -> "SOBEI".equalsIgnoreCase(i.getTipoOsc()))
                .count();
        long totalOutrasOsc = totalInscritos - totalSobei;

        double percentualSobei = totalInscritos > 0
                ? Math.round(((double) totalSobei / totalInscritos * 100) * 10.0) / 10.0
                : 0.0;
        double percentualOutrasOsc = totalInscritos > 0
                ? Math.round(((double) totalOutrasOsc / totalInscritos * 100) * 10.0) / 10.0
                : 0.0;

        long totalComOficina = todas.stream()
                .filter(i -> (i.getOficina() != null && !i.getOficina().isBlank()) ||
                             (i.getOficinaManha() != null && !i.getOficinaManha().isBlank()) ||
                             (i.getOficinaTarde() != null && !i.getOficinaTarde().isBlank()))
                .count();
        long totalSemOficina = totalInscritos - totalComOficina;

        double percentualComOficina = totalInscritos > 0
                ? Math.round(((double) totalComOficina / totalInscritos * 100) * 10.0) / 10.0
                : 0.0;
        double percentualSemOficina = totalInscritos > 0
                ? Math.round(((double) totalSemOficina / totalInscritos * 100) * 10.0) / 10.0
                : 0.0;

        long presentesGeral = todas.stream()
                .filter(i -> Boolean.TRUE.equals(i.getPresente()))
                .count();
        long presentesDia11 = todas.stream()
                .filter(i -> Boolean.TRUE.equals(i.getPresenteDia11()))
                .count();
        long presentesDia12 = todas.stream()
                .filter(i -> Boolean.TRUE.equals(i.getPresenteDia12()))
                .count();
        long presentesAmbosDias = todas.stream()
                .filter(i -> Boolean.TRUE.equals(i.getPresenteDia11()) && Boolean.TRUE.equals(i.getPresenteDia12()))
                .count();

        // 1. Unidades SOBEI
        Map<String, List<InscricaoCongresso>> porUnidadeMap = todas.stream()
                .filter(i -> "SOBEI".equalsIgnoreCase(i.getTipoOsc()))
                .collect(Collectors.groupingBy(i -> {
                    String u = i.getUnidade();
                    if (u == null || u.isBlank()) return "Não informada";
                    String norm = u.trim();
                    if (!norm.toUpperCase().startsWith("CEI ") && !norm.toUpperCase().startsWith("CEDESP") && !norm.toUpperCase().startsWith("CCINTER") && !norm.toUpperCase().startsWith("NCI") && !norm.toUpperCase().startsWith("MATRIZ")) {
                        return "CEI " + norm;
                    }
                    return norm;
                }));

        List<EstatisticaCongressoResponse.UnidadeCongressoStat> porUnidade = porUnidadeMap.entrySet().stream()
                .map(e -> {
                    String nomeUnidade = e.getKey();
                    long total = e.getValue().size();
                    long comOf = e.getValue().stream().filter(i -> (i.getOficina() != null && !i.getOficina().isBlank()) || (i.getOficinaManha() != null && !i.getOficinaManha().isBlank())).count();
                    long semOf = total - comOf;
                    double part = totalSobei > 0 ? Math.round(((double) total / totalSobei * 100) * 10.0) / 10.0 : 0.0;
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
        List<EstatisticaCongressoResponse.OficinaCongressoStat> porOficina = OFICINAS_CATALOGO.stream()
                .map(ofMeta -> {
                    List<InscricaoCongresso> inscritosNestaOficina = todas.stream()
                            .filter(i -> {
                                String of = i.getOficina() != null ? i.getOficina() : (i.getOficinaManha() != null ? i.getOficinaManha() : i.getOficinaTarde());
                                return isOficinaMatch(of, ofMeta);
                            })
                            .toList();

                    long totalOf = inscritosNestaOficina.size();
                    long sobeiOf = inscritosNestaOficina.stream().filter(i -> "SOBEI".equalsIgnoreCase(i.getTipoOsc())).count();
                    long outrasOf = totalOf - sobeiOf;
                    int capacidade = ofMeta.capacidade();
                    int restantes = Math.max(0, capacidade - (int) totalOf);
                    double taxaOcup = Math.round(((double) totalOf / capacidade * 100) * 10.0) / 10.0;
                    String status = taxaOcup >= 100.0 ? "ESGOTADA" : (taxaOcup >= 80.0 ? "QUASE_CHEIA" : "DISPONIVEL");

                    return EstatisticaCongressoResponse.OficinaCongressoStat.builder()
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
                            .build();
                })
                .sorted(Comparator.comparingLong(EstatisticaCongressoResponse.OficinaCongressoStat::getTotalInscritos).reversed())
                .collect(Collectors.toList());

        // 3. Outras OSCs (unificando pequenas variações de nomes como CT-Vidas, CT Vidas, etc.)
        List<EstatisticaCongressoResponse.OutraOscStat> porOutraOsc = agruparOutrasOscs(todas, totalOutrasOsc);

        // 4. Evolução Temporal (Gráfico de Crescimento)
        DateTimeFormatter dtfDiaMes = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter dtfIso = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<LocalDate, List<InscricaoCongresso>> porDataMap = todas.stream()
                .collect(Collectors.groupingBy(i -> {
                    if (i.getDataInscricao() != null) {
                        return i.getDataInscricao().toLocalDate();
                    }
                    return LocalDate.now();
                }));

        List<LocalDate> datasOrdenadas = porDataMap.keySet().stream()
                .sorted()
                .toList();

        List<EstatisticaCongressoResponse.EvolucaoInscricaoStat> evolucao = new ArrayList<>();
        long acumulador = 0;
        for (LocalDate d : datasOrdenadas) {
            List<InscricaoCongresso> inscritosNoDia = porDataMap.get(d);
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

    private boolean isOficinaMatch(String ofInscrito, OficinaMetadata ofMeta) {
        if (ofInscrito == null || ofInscrito.isBlank()) return false;
        String chave = OficinaCotasConfig.normalizarTexto(ofInscrito);
        String chaveTema = OficinaCotasConfig.normalizarTexto(ofMeta.tema());
        String chaveMin = OficinaCotasConfig.normalizarTexto(ofMeta.ministrante());
        if (chave.equals(chaveTema) || chave.equals(chaveMin)) return true;
        if (chave.contains(chaveTema) || chaveTema.contains(chave)) return true;
        if (chaveMin.length() >= 8 && (chave.contains(chaveMin) || chaveMin.contains(chave))) return true;
        return false;
    }

    /**
     * Unifica variações e pequenas diferenças no nome de outras instituições (ex: CT-Vidas, CT Vidas, CTVidas, etc.)
     */
    private List<EstatisticaCongressoResponse.OutraOscStat> agruparOutrasOscs(List<InscricaoCongresso> todas, long totalOutrasOsc) {
        List<InscricaoCongresso> outrasInscricoes = todas.stream()
                .filter(i -> !"SOBEI".equalsIgnoreCase(i.getTipoOsc()))
                .toList();

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

        List<EstatisticaCongressoResponse.OutraOscStat> resultado = new ArrayList<>();
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
        String semAcento = Normalizer.normalize(raw.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();

        // 2. Se houver sigla/código entre parênteses, ex: 'Centro de Treinamento (CT-Vidas)'
        Matcher parenMatcher = Pattern.compile("\\((.*?)\\)").matcher(semAcento);
        if (parenMatcher.find()) {
            String inside = parenMatcher.group(1).replaceAll("[^a-z0-9]", "");
            if (inside.length() >= 2 && inside.length() <= 12) {
                return inside;
            }
        }

        // 3. Se houver separador de cláusula com espaços, ex: 'CTVidas - Centro de Treinamento das Vidas'
        String[] partes = semAcento.split("\\s+[-/|:]\\s+");
        String principal = partes[0].trim();

        // 4. Remover pontuação e manter apenas caracteres alfanuméricos
        String alfa = principal.replaceAll("[^a-z0-9]", "");
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
                    if (a.matches(".*\\b[A-Z]{2,}\\b.*")) scoreA += 3L;
                    if (b.matches(".*\\b[A-Z]{2,}\\b.*")) scoreB += 3L;

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

