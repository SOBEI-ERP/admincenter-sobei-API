package br.org.sobei.denuncias.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedidaAdotadaRequest {
    private Integer id;
    private String descricao;
}
