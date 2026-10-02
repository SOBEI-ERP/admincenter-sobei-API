package br.org.sobei.denuncias.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BancoTalentoVagaResponse {
    private Integer vagaId;
    private String vagaTitulo;
    private String vagaUnidade;
    private long totalTalentos;
    private LocalDateTime ultimaMovimentacao;
}
