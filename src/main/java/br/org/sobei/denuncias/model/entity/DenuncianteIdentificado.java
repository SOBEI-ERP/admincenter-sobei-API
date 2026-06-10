package br.org.sobei.denuncias.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "denunciantes_identificados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DenuncianteIdentificado {

    @Id
    @Column(name = "denuncia_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "denuncia_id")
    private Denuncia denuncia;

    @Column(name = "nome_completo", nullable = false, length = 255)
    private String nomeCompleto;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefone;
}
