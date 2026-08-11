package br.org.sobei.denuncias.dto.request;

import br.org.sobei.denuncias.model.enums.PrioridadeChamado;
import br.org.sobei.denuncias.model.enums.StatusChamado;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtualizarChamadoRequest {

    private String titulo;
    private String descricao;
    private String solicitante;
    private PrioridadeChamado prioridade;
    private StatusChamado status;
    private LocalDate prazoConclusao;
    private String planoAcao;
    private String resolucao;
}
