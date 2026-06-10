package br.org.sobei.denuncias.model.entity;

import br.org.sobei.denuncias.model.enums.TipoConclusao;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "denuncia_id")
    private Denuncia denuncia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Usuario admin;

    @Column(name = "tipo_conclusao", nullable = false)
    private TipoConclusao tipoConclusao;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String relatorio;

    @CreationTimestamp
    @Column(name = "data_conclusao", updatable = false)
    private LocalDateTime dataConclusao;
}
