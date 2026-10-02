package br.org.sobei.denuncias.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultarInscricaoRequest {

    @NotBlank(message = "O CPF é obrigatório")
    @Schema(description = "CPF do participante (com ou sem pontuação)", example = "123.456.789-00")
    private String cpf;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    @Schema(description = "E-mail cadastrado na inscrição", example = "maria.silva@exemplo.com")
    private String email;
}
