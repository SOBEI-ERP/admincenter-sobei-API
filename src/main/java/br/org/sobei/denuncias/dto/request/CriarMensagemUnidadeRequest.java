package br.org.sobei.denuncias.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriarMensagemUnidadeRequest {

    @NotBlank(message = "A unidade é obrigatória")
    @Size(max = 100, message = "A unidade não pode ter mais que 100 caracteres")
    private String unidade;

    @NotBlank(message = "O nome completo é obrigatório")
    @Size(max = 255, message = "O nome não pode ter mais que 255 caracteres")
    private String nomeCompleto;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    @Size(max = 150, message = "O e-mail não pode ter mais que 150 caracteres")
    private String email;

    @NotBlank(message = "O telefone é obrigatório")
    @Size(max = 20, message = "O telefone não pode ter mais que 20 caracteres")
    private String telefone;

    @NotBlank(message = "A mensagem é obrigatória")
    private String mensagem;
}
