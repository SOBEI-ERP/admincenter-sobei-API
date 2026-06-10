package br.org.sobei.denuncias.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class EstatisticaResponse {
    private Map<String, Long> porUnidade;
    private DistribuicaoStats distribuicao;

    @Data
    @Builder
    public static class DistribuicaoStats {
        private List<LabelValue> tipos;
        private List<LabelValue> status;
    }

    @Data
    @Builder
    public static class LabelValue {
        private String name;
        private Long value;
    }
}
