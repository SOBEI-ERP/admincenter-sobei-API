package br.org.sobei.denuncias.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "O usuário é obrigatório")
    private String usuario;

    @NotBlank(message = "A senha é obrigatória")
    private String senha;
}
