package br.org.sobei.denuncias.dto.request;

import br.org.sobei.denuncias.model.enums.PrioridadeChamado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriarChamadoRequest {

    @NotBlank(message = "O título do chamado é obrigatório")
    @Size(max = 255, message = "O título não pode ter mais que 255 caracteres")
    private String titulo;

    @NotBlank(message = "A descrição detalhada do chamado é obrigatória")
    private String descricao;

    @NotBlank(message = "O nome ou departamento do solicitante é obrigatório")
    @Size(max = 255, message = "O solicitante não pode ter mais que 255 caracteres")
    private String solicitante;

    private PrioridadeChamado prioridade;

    private LocalDate prazoConclusao;

    private String planoAcao;
}
