package br.org.sobei.denuncias.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedidaAdotadaResponse {
    private Integer id;
    private String descricao;
    private LocalDateTime dataRegistro;
    private String autor;
}
