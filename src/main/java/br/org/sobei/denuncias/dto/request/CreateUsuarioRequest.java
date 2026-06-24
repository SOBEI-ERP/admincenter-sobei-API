package br.org.sobei.denuncias.dto.request;

import br.org.sobei.denuncias.model.enums.NivelAdmin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUsuarioRequest {

    @NotBlank(message = "O usuário é obrigatório")
    private String usuario;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "O email deve ser válido")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    private String senha;

    @NotNull(message = "O nível é obrigatório")
    private NivelAdmin nivel;

    private String unidade;
}
