package br.org.sobei.denuncias.dto.request;

import br.org.sobei.denuncias.model.enums.TipoDenuncia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CriarDenunciaRequest {

    @NotNull(message = "O tipo da denúncia é obrigatório")
    private TipoDenuncia tipo;

    @NotBlank(message = "A unidade é obrigatória")
    @Size(max = 100, message = "A unidade não pode ter mais de 100 caracteres")
    private String unidade;

    @NotBlank(message = "A descrição é obrigatória")
    private String descricao;

    private String envolvidos;

    private String testemunhas;

    // Dados opcionais do denunciante
    private String nomeCompleto;
    private String email;
    private String telefone;
}
