package br.org.sobei.denuncias.dto.request;

import br.org.sobei.denuncias.model.enums.TipoDenuncia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CriarDenunciaRequest {

    @NotNull(message = "O tipo da denúncia é obrigatório")
    private TipoDenuncia tipo;

    @NotBlank(message = "A unidade é obrigatória")
    @Size(max = 100, message = "A unidade não pode ter mais de 100 caracteres")
    private String unidade;

    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 5000, message = "A descrição não pode ter mais de 5000 caracteres")
    private String descricao;

    @Size(max = 1000, message = "O campo envolvidos não pode ter mais de 1000 caracteres")
    private String envolvidos;

    @Size(max = 1000, message = "O campo testemunhas não pode ter mais de 1000 caracteres")
    private String testemunhas;

    // Dados opcionais do denunciante
    @Size(max = 150, message = "O nome completo não pode ter mais de 150 caracteres")
    private String nomeCompleto;

    @Size(max = 150, message = "O email não pode ter mais de 150 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9-]{2,}\\.[a-zA-Z0-9-]{2,}(\\.[a-zA-Z0-9-]{2,})?$", message = "Informe um email válido com domínio existente")
    private String email;

    @Size(max = 20, message = "O telefone não pode ter mais de 20 caracteres")
    @Pattern(regexp = "^\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}$", message = "Informe um telefone válido no formato (XX) XXXXX-XXXX")
    private String telefone;
}
