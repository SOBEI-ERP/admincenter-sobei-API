package br.org.sobei.denuncias.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inscricoes_congresso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscricaoCongresso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nome_completo", nullable = false, length = 255)
    private String nomeCompleto;

    @Column(nullable = false, length = 14)
    private String cpf;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "tipo_osc", nullable = false, length = 20)
    private String tipoOsc; // "SOBEI" ou "OUTRA"

    @Column(length = 100)
    private String unidade; // Unidade SOBEI

    @Column(name = "outra_osc", length = 255)
    private String outraOsc; // Nome da outra OSC

    @Column(nullable = false)
    @Builder.Default
    private Boolean presente = false;

    @Column(name = "presente_dia11", nullable = false)
    @Builder.Default
    private Boolean presenteDia11 = false;

    @Column(name = "data_presenca_dia11")
    private LocalDateTime dataPresencaDia11;

    @Column(name = "presente_dia12", nullable = false)
    @Builder.Default
    private Boolean presenteDia12 = false;

    @Column(name = "data_presenca_dia12")
    private LocalDateTime dataPresencaDia12;

    @Column(name = "oficina", length = 255)
    private String oficina;

    @Column(name = "oficina_manha", length = 255)
    private String oficinaManha;

    @Column(name = "oficina_tarde", length = 255)
    private String oficinaTarde;

    @CreationTimestamp
    @Column(name = "data_inscricao", updatable = false)
    private LocalDateTime dataInscricao;

    @Column(name = "data_presenca")
    private LocalDateTime dataPresenca;
}
