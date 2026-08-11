package br.org.sobei.denuncias.model.entity;

import br.org.sobei.denuncias.model.enums.PrioridadeChamado;
import br.org.sobei.denuncias.model.enums.StatusChamado;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "chamados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, length = 255)
    private String solicitante;

    @Builder.Default
    @Column(nullable = false)
    private PrioridadeChamado prioridade = PrioridadeChamado.MEDIA;

    @Builder.Default
    @Column(nullable = false)
    private StatusChamado status = StatusChamado.ABERTO;

    @Column(name = "prazo_conclusao")
    private LocalDate prazoConclusao;

    @Column(name = "plano_acao", columnDefinition = "TEXT")
    private String planoAcao;

    @Column(columnDefinition = "TEXT")
    private String resolucao;

    @Column(name = "data_encerramento")
    private LocalDateTime dataEncerramento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @CreationTimestamp
    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "ultima_alteracao")
    private LocalDateTime ultimaAlteracao;
}
