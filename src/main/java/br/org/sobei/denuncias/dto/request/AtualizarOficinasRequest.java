package br.org.sobei.denuncias.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarOficinasRequest {

    @Size(max = 255, message = "O nome da oficina não pode ultrapassar 255 caracteres.")
    private String oficina;

    @Size(max = 255, message = "O nome da oficina da manhã não pode ultrapassar 255 caracteres.")
    private String oficinaManha;

    @Size(max = 255, message = "O nome da oficina da tarde não pode ultrapassar 255 caracteres.")
    private String oficinaTarde;
}
