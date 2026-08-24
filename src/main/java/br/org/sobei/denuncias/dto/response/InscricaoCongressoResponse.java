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
public class InscricaoCongressoResponse {

    private Integer id;
    private String nomeCompleto;
    private String cpf;
    private String email;
    private String tipoOsc;
    private String unidade;
    private String outraOsc;
    private Boolean presente;
    private Boolean presenteDia11;
    private LocalDateTime dataPresencaDia11;
    private Boolean presenteDia12;
    private LocalDateTime dataPresencaDia12;
    private String oficinaManha;
    private String oficinaTarde;
    private LocalDateTime dataInscricao;
    private LocalDateTime dataPresenca;
}
