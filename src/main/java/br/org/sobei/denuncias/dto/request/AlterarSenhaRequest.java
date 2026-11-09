package br.org.sobei.denuncias.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @Size(min = 8, message = "A senha deve conter no mínimo 8 caracteres")
    private String senha;
}
