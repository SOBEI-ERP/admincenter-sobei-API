package br.org.sobei.denuncias.model.entity;

import br.org.sobei.denuncias.model.enums.ModalidadeVaga;
import br.org.sobei.denuncias.model.enums.StatusVaga;
import br.org.sobei.denuncias.model.enums.TipoContrato;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vagas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(nullable = false, length = 100)
    private String departamento;

    @Column(nullable = false, length = 100)
    private String unidade;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String requisitos;

    @Column(columnDefinition = "TEXT")
    private String beneficios;

    @Column(nullable = false)
    private ModalidadeVaga modalidade;

    @Column(name = "tipo_contrato", nullable = false)
    private TipoContrato tipoContrato;

    @Column(nullable = false)
    private StatusVaga status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Usuario admin;

    @OneToMany(mappedBy = "vaga", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Candidatura> candidaturas = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
}
