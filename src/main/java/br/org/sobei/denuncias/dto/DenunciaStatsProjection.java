package br.org.sobei.denuncias.dto;

import br.org.sobei.denuncias.model.enums.StatusDenuncia;
import br.org.sobei.denuncias.model.enums.TipoDenuncia;

public interface DenunciaStatsProjection {
    String getUnidade();
    TipoDenuncia getTipo();
    StatusDenuncia getEstado();
}
