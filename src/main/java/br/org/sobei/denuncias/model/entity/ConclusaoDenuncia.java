package br.org.sobei.denuncias.model.entity;

import br.org.sobei.denuncias.model.enums.TipoConclusao;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "conclusoes_denuncia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConclusaoDenuncia {

    @Id
    @Column(name = "denuncia_id")
    private Integer denunciaId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "denuncia_id")
    private Denuncia denuncia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Usuario administrador;

    @Column(name = "tipo_conclusao", nullable = false)
    private TipoConclusao tipoConclusao;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String relatorio;

    @Column(name = "data_conclusao", insertable = false, updatable = false)
    private Instant dataConclusao;
}
