package br.org.sobei.denuncias.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriarDenunciaResponse {
    private String protocolo;
    private String mensagem;
}
