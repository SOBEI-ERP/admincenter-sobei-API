package br.org.sobei.denuncias.dto.response;

import br.org.sobei.denuncias.model.enums.NivelAdmin;
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
public class UsuarioDto {
    private Integer id;
    private String usuario;
    private String email;
    private NivelAdmin nivel;
}
