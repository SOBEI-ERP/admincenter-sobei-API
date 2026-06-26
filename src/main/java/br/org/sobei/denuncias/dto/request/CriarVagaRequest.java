package br.org.sobei.denuncias.dto.request;

import br.org.sobei.denuncias.model.enums.ModalidadeVaga;
import br.org.sobei.denuncias.model.enums.TipoContrato;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CriarVagaRequest {

    @NotBlank(message = "O título é obrigatório")
    private String titulo;

    @NotBlank(message = "O departamento é obrigatório")
    private String departamento;

    @NotBlank(message = "A descrição é obrigatória")
    private String descricao;

    @NotBlank(message = "Os requisitos são obrigatórios")
    private String requisitos;

    private String beneficios;

    @NotNull(message = "A modalidade é obrigatória")
    private ModalidadeVaga modalidade;

    @NotNull(message = "O tipo de contrato é obrigatório")
    private TipoContrato tipoContrato;

    private String unidade;
}
