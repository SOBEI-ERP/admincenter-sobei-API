package br.org.sobei.denuncias.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensagemUnidadeResponse {

    private Integer id;
    private String unidade;
    private String nomeCompleto;
    private String email;
    private String telefone;
    private String mensagem;
    private Boolean lida;
    private LocalDateTime dataEnvio;
}
