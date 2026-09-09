package br.org.sobei.denuncias.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EstatisticaCongressoResponse {

    // 1. Informações gerais de inscrição
    private long totalInscritos;
    private long limiteVagas; // 900
    private double percentualPreenchimento;
    private long totalSobei;
    private long totalOutrasOsc;
    private double percentualSobei;
    private double percentualOutrasOsc;
    private long totalComOficina;
    private long totalSemOficina;
    private double percentualComOficina;
    private double percentualSemOficina;

    // Presença / Check-ins
    private long presentesGeral;
    private long presentesDia11;
    private long presentesDia12;
    private long presentesAmbosDias;

    // 2. Inscrição por unidade SOBEI
    private List<UnidadeCongressoStat> porUnidade;

    // 3. Inscrições por oficina (23 oficinas)
    private List<OficinaCongressoStat> porOficina;

    // 4. Detalhamento de Outras OSCs
    private List<OutraOscStat> porOutraOsc;

    // 5. Gráficos de crescimento nas inscrições (Evolução temporal)
    private List<EvolucaoInscricaoStat> evolucaoInscricoes;

    @Getter
    @Builder
    public static class UnidadeCongressoStat {
        private String unidade;
        private long totalInscritos;
        private long comOficina;
        private long semOficina;
        private double percentualDoTotal;
    }

    @Getter
    @Builder
    public static class OficinaCongressoStat {
        private String id;
        private String tema;
        private String ministrante;
        private String categoria;
        private int capacidadeSala;
        private long totalInscritos;
        private long inscritosSobei;
        private long inscritosOutrasOsc;
        private int vagasRestantes;
        private double percentualOcupacao;
        private String status; // "DISPONIVEL", "QUASE_CHEIA", "ESGOTADA"
    }

    @Getter
    @Builder
    public static class OutraOscStat {
        private String nomeOsc;
        private long totalInscritos;
        private double percentualOutras;
    }

    @Getter
    @Builder
    public static class EvolucaoInscricaoStat {
        private String data; // "DD/MM"
        private String dataCompleta; // "YYYY-MM-DD"
        private long noDia;
        private long acumulado;
        private long sobeiNoDia;
        private long outrasOscNoDia;
    }
}
