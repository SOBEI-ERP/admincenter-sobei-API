package br.org.sobei.denuncias.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidaturas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidatura {

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

    @CreationTimestamp
    @Column(name = "data_envio", updatable = false)
    private LocalDateTime dataEnvio;
}
