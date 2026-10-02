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
public class BancoTalentoResponse {
    private Integer id;
    private String nomeCompleto;
    private String email;
    private String telefone;
    private String cartaApresentacao;
    private String curriculoNome;
    private LocalDateTime dataEnvioOriginal;
    private LocalDateTime dataMovimentacao;
}
