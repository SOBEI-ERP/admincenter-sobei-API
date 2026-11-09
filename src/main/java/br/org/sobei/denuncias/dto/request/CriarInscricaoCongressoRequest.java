package br.org.sobei.denuncias.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriarInscricaoCongressoRequest {

    @NotBlank(message = "O nome completo é obrigatório")
    @Size(max = 255, message = "O nome completo não pode ter mais que 255 caracteres")
    private String nomeCompleto;

    @NotBlank(message = "O CPF é obrigatório")
    @Size(max = 14, message = "O CPF não pode ter mais que 14 caracteres")
    private String cpf;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "O email deve ser válido")
    @Size(max = 150, message = "O email não pode ter mais que 150 caracteres")
    private String email;

    @NotBlank(message = "O tipo de OSC é obrigatório (SOBEI ou OUTRA)")
    @Size(max = 20, message = "O tipo de OSC não pode ter mais que 20 caracteres")
    private String tipoOsc;

    @Size(max = 100, message = "A unidade não pode ter mais que 100 caracteres")
    private String unidade;

    @Size(max = 255, message = "O nome da outra OSC não pode ter mais que 255 caracteres")
    private String outraOsc;
}
