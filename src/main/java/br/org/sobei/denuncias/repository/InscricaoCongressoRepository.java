package br.org.sobei.denuncias.repository;

import br.org.sobei.denuncias.model.entity.InscricaoCongresso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InscricaoCongressoRepository extends JpaRepository<InscricaoCongresso, Integer> {

    List<InscricaoCongresso> findAllByOrderByNomeCompletoAsc();

    Optional<InscricaoCongresso> findByCpf(String cpf);

    Optional<InscricaoCongresso> findByEmailIgnoreCase(String email);

    @Query("SELECT i FROM InscricaoCongresso i WHERE " +
           "(i.cpf = :cpf OR REPLACE(REPLACE(i.cpf, '.', ''), '-', '') = :cpfSemMascara) AND " +
           "LOWER(TRIM(i.email)) = LOWER(TRIM(:email))")
    Optional<InscricaoCongresso> findByCpfAndEmail(
            @Param("cpf") String cpf,
            @Param("cpfSemMascara") String cpfSemMascara,
            @Param("email") String email
    );

    @Query("SELECT i FROM InscricaoCongresso i WHERE " +
           "(:termo IS NULL OR LOWER(i.nomeCompleto) LIKE LOWER(CONCAT('%', :termo, '%')) OR i.cpf LIKE CONCAT('%', :termo, '%') OR (:termoCpf IS NOT NULL AND REPLACE(REPLACE(i.cpf, '.', ''), '-', '') LIKE CONCAT('%', :termoCpf, '%'))) AND " +
           "(:unidade IS NULL OR LOWER(i.unidade) = LOWER(:unidade)) AND " +
           "(:tipoOsc IS NULL OR LOWER(i.tipoOsc) = LOWER(:tipoOsc)) AND " +
           "(:presente IS NULL OR i.presente = :presente) " +
           "ORDER BY i.nomeCompleto ASC")
    List<InscricaoCongresso> buscarComFiltros(
            @Param("termo") String termo,
            @Param("termoCpf") String termoCpf,
            @Param("unidade") String unidade,
            @Param("tipoOsc") String tipoOsc,
            @Param("presente") Boolean presente
    );

    @Query("SELECT i FROM InscricaoCongresso i WHERE " +
           "LOWER(i.tipoOsc) = 'sobei' AND LOWER(i.unidade) = LOWER(:unidade) AND " +
           "(:termo IS NULL OR LOWER(i.nomeCompleto) LIKE LOWER(CONCAT('%', :termo, '%')) OR i.cpf LIKE CONCAT('%', :termo, '%') OR (:termoCpf IS NOT NULL AND REPLACE(REPLACE(i.cpf, '.', ''), '-', '') LIKE CONCAT('%', :termoCpf, '%'))) AND " +
           "(:presente IS NULL OR i.presente = :presente) " +
           "ORDER BY i.nomeCompleto ASC")
    List<InscricaoCongresso> buscarPorUnidadeCoordenadora(
            @Param("unidade") String unidade,
            @Param("termo") String termo,
            @Param("termoCpf") String termoCpf,
            @Param("presente") Boolean presente
    );
}
