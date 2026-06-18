package br.org.sobei.denuncias.dto.response;

import br.org.sobei.denuncias.model.enums.StatusDenuncia;
import br.org.sobei.denuncias.model.enums.TipoDenuncia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaProtocoloResponse {
    private String protocolo;
    private StatusDenuncia estado;
    private LocalDateTime dataAbertura;
    private LocalDateTime ultimaAlteracao;
    private TipoDenuncia tipo;
    private String unidade;
    private String descricao;
    private String envolvidos;
    private String testemunhas;
    private String relatorioConclusao;
    private String tipoConclusao;
}
