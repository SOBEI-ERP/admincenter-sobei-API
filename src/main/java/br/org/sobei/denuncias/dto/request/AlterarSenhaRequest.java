package br.org.sobei.denuncias.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlterarSenhaRequest {

    @NotBlank(message = "A senha é obrigatória")
    private String senha;
}
