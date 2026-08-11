package br.org.sobei.denuncias.dto.response;

import br.org.sobei.denuncias.model.enums.PrioridadeChamado;
import br.org.sobei.denuncias.model.enums.StatusChamado;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ChamadoResponse {

    private Integer id;
    private String titulo;
    private String descricao;
    private String solicitante;
    private PrioridadeChamado prioridade;
    private StatusChamado status;
    private LocalDate prazoConclusao;
    private String planoAcao;
    private String resolucao;
    private LocalDateTime dataEncerramento;
    private String criadoPor;
    private LocalDateTime dataCriacao;
    private LocalDateTime ultimaAlteracao;
}
