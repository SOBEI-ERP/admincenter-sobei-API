package br.org.sobei.denuncias.dto.request;

import br.org.sobei.denuncias.model.enums.PrioridadeDenuncia;
import br.org.sobei.denuncias.model.enums.StatusDenuncia;
import br.org.sobei.denuncias.model.enums.TipoConclusao;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AtualizarDenunciaRequest {
    
    @NotNull(message = "O status é obrigatório")
    private StatusDenuncia status;
    
    // Usado se for tomada alguma medida
    /**
     * @deprecated Use a lista de {@code medidas} para registrar ações tomadas.
     * Mantido para compatibilidade retroativa com versões antigas do frontend.
     */
    @Deprecated
    private String descricaoAcao;
    private java.util.List<MedidaAdotadaRequest> medidas;
    
    // Usado se for fechar ou arquivar
    private String relatorio;
    private TipoConclusao tipoConclusao;

    // Prioridade da denúncia
    private PrioridadeDenuncia prioridade;
}
