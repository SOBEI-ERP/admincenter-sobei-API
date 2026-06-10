package br.org.sobei.denuncias.model.entity;

import br.org.sobei.denuncias.model.enums.StatusDenuncia;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "historico_estados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "denuncia_id", nullable = false)
    private Denuncia denuncia;

    @Column(name = "estado_anterior")
    private StatusDenuncia estadoAnterior;

    @Column(name = "estado_novo", nullable = false)
    private StatusDenuncia estadoNovo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Usuario administrador;

    @Column(name = "data_alteracao", insertable = false, updatable = false)
    private Instant dataAlteracao;
}
