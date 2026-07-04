package br.org.sobei.denuncias.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriarDenunciaResponse {
    private String protocolo;
    private String mensagem;
}
