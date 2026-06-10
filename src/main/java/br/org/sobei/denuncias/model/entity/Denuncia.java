package br.org.sobei.denuncias.model.entity;

import br.org.sobei.denuncias.model.enums.StatusDenuncia;
import br.org.sobei.denuncias.model.enums.TipoDenuncia;
import br.org.sobei.denuncias.model.enums.Unidade;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "denuncias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Denuncia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String protocolo;

    @Column(nullable = false)
    private TipoDenuncia tipo;

    @Column(nullable = false, length = 100)
    private Unidade unidade;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(columnDefinition = "TEXT")
    private String envolvidos;

    @Column(columnDefinition = "TEXT")
    private String testemunhas;

    @Column(nullable = false)
    private StatusDenuncia estado;

    @Column(name = "data_abertura", insertable = false, updatable = false)
    private Instant dataAbertura;

    @Column(name = "ultima_alteracao", insertable = false, updatable = false)
    private Instant ultimaAlteracao;

    // Relationships
    @OneToOne(mappedBy = "denuncia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private DenuncianteIdentificado denunciante;

    @OneToOne(mappedBy = "denuncia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ConclusaoDenuncia conclusao;

    @OneToMany(mappedBy = "denuncia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MedidaAdotada> medidasAdotadas;

    @OneToMany(mappedBy = "denuncia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HistoricoEstado> historicoEstados;
}
