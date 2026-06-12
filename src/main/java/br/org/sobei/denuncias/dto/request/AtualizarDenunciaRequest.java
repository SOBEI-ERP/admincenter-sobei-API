package br.org.sobei.denuncias.dto.request;

import br.org.sobei.denuncias.model.enums.StatusDenuncia;
import br.org.sobei.denuncias.model.enums.TipoConclusao;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AtualizarDenunciaRequest {
    
    @NotNull(message = "O status é obrigatório")
    private StatusDenuncia status;
    
    // Usado se for tomada alguma medida
    private String descricaoAcao;
    private java.util.List<MedidaAdotadaRequest> medidas;
    
    // Usado se for fechar ou arquivar
    private String relatorio;
    private TipoConclusao tipoConclusao;
}
