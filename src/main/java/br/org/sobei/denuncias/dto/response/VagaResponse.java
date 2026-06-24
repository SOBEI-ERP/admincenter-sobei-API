package br.org.sobei.denuncias.dto.response;

import br.org.sobei.denuncias.model.enums.ModalidadeVaga;
import br.org.sobei.denuncias.model.enums.StatusVaga;
import br.org.sobei.denuncias.model.enums.TipoContrato;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VagaResponse {
    private Integer id;
    private String titulo;
    private String departamento;
    private String unidade;
    private String descricao;
    private String requisitos;
    private String beneficios;
    private ModalidadeVaga modalidade;
    private TipoContrato tipoContrato;
    private StatusVaga status;
    private Integer totalCandidaturas;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}
