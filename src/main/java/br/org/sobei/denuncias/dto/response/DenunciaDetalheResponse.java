package br.org.sobei.denuncias.dto.response;

import br.org.sobei.denuncias.model.enums.StatusDenuncia;
import br.org.sobei.denuncias.model.enums.TipoDenuncia;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DenunciaDetalheResponse {
    private Integer id;
    private String protocolo;
    private StatusDenuncia status;
    private TipoDenuncia tipo;
    private String unidade;
    private LocalDateTime dataEnvio;
    private String descricao;
    private String envolvidos;
    private String testemunhas;
    
    // Dados do Denunciante (se identificado)
    private String nomeDenunciante;
    private String emailDenunciante;
    private String telefoneDenunciante;
    
    // Timeline e Medidas
    private List<MedidaAdotadaResponse> medidasAdotadas;
    private String relatorioConclusao;
    private String tipoConclusao;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    private LocalDateTime dataArquivamento;
}
