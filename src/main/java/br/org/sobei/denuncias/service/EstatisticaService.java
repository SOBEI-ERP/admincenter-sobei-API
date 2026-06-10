package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.response.EstatisticaResponse;
import br.org.sobei.denuncias.model.entity.Denuncia;
import br.org.sobei.denuncias.model.enums.TipoDenuncia;
import br.org.sobei.denuncias.repository.DenunciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstatisticaService {

    private final DenunciaRepository denunciaRepository;

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

        List<Denuncia> todas = denunciaRepository.findAll(spec);

        Map<String, Long> porUnidade = todas.stream()
                .filter(d -> d.getUnidade() != null)
                .collect(Collectors.groupingBy(Denuncia::getUnidade, Collectors.counting()));

        Map<String, Long> porTipo = todas.stream()
                .collect(Collectors.groupingBy(d -> d.getTipo().name(), Collectors.counting()));

        Map<String, Long> porStatus = todas.stream()
                .collect(Collectors.groupingBy(d -> d.getEstado().name(), Collectors.counting()));

        var tiposList = porTipo.entrySet().stream()
                .map(e -> EstatisticaResponse.LabelValue.builder().name(e.getKey()).value(e.getValue()).build())
                .collect(Collectors.toList());

        var statusList = porStatus.entrySet().stream()
                .map(e -> EstatisticaResponse.LabelValue.builder().name(e.getKey()).value(e.getValue()).build())
                .collect(Collectors.toList());

        return EstatisticaResponse.builder()
                .porUnidade(porUnidade)
                .distribuicao(EstatisticaResponse.DistribuicaoStats.builder()
                        .tipos(tiposList)
                        .status(statusList)
                        .build())
                .build();
    }
}
