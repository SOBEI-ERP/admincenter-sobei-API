package br.org.sobei.denuncias.dto.response;

import br.org.sobei.denuncias.model.enums.PrioridadeDenuncia;
import br.org.sobei.denuncias.model.enums.StatusDenuncia;
import br.org.sobei.denuncias.model.enums.TipoDenuncia;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DenunciaAdminResponse {
    private Integer id;
    private String protocolo;
    private StatusDenuncia status;
    private TipoDenuncia tipo;
    private String unidade;
    private LocalDateTime dataEnvio;
    private LocalDateTime dataAbertura;
    private LocalDateTime ultimaAlteracao;
    private LocalDateTime dataFechamento;
    private LocalDateTime dataArquivamento;
    private PrioridadeDenuncia prioridade;
}
