package br.org.sobei.denuncias.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedidaAdotadaResponse {
    private Integer id;
    private String descricao;
    private LocalDateTime dataRegistro;
    private String autor;
}
