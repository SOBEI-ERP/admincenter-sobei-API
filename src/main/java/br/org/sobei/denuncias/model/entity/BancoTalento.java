package br.org.sobei.denuncias.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "banco_talentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BancoTalento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaga_id", nullable = false)
    private Vaga vaga;

    @Column(name = "nome_completo", nullable = false, length = 255)
    private String nomeCompleto;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(name = "carta_apresentacao", columnDefinition = "TEXT")
    private String cartaApresentacao;

    @Column(name = "curriculo_path", nullable = false, length = 500)
    private String curriculoPath;

    @Column(name = "curriculo_nome", nullable = false, length = 255)
    private String curriculoNome;

    @Column(name = "data_envio_original", nullable = false)
    private LocalDateTime dataEnvioOriginal;

    @CreationTimestamp
    @Column(name = "data_movimentacao", updatable = false)
    private LocalDateTime dataMovimentacao;
}
