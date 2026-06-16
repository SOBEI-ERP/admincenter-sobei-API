package br.org.sobei.denuncias.dto;

import br.org.sobei.denuncias.model.enums.PrioridadeDenuncia;
import br.org.sobei.denuncias.model.enums.StatusDenuncia;
import br.org.sobei.denuncias.model.enums.TipoDenuncia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DenunciaStatsDto {
    private String unidade;
    private TipoDenuncia tipo;
    private StatusDenuncia estado;
    private PrioridadeDenuncia prioridade;
}
