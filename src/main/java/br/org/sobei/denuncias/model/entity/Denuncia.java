package br.org.sobei.denuncias.model.entity;

import br.org.sobei.denuncias.model.enums.StatusDenuncia;
import br.org.sobei.denuncias.model.enums.TipoDenuncia;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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
    private String unidade;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(columnDefinition = "TEXT")
    private String envolvidos;

    @Column(columnDefinition = "TEXT")
    private String testemunhas;

    @Builder.Default
    @Column(nullable = false)
    private StatusDenuncia estado = StatusDenuncia.NA_FILA;

    @CreationTimestamp
    @Column(name = "data_abertura", updatable = false)
    private LocalDateTime dataAbertura;

    @UpdateTimestamp
    @Column(name = "ultima_alteracao")
    private LocalDateTime ultimaAlteracao;

    @OneToOne(mappedBy = "denuncia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private DenuncianteIdentificado denunciante;

    @OneToOne(mappedBy = "denuncia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ConclusaoDenuncia conclusao;

    @Builder.Default
    @OneToMany(mappedBy = "denuncia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<HistoricoEstado> historicos = new java.util.ArrayList<>();
}
