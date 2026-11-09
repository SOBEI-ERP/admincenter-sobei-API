package br.org.sobei.denuncias.repository;

import br.org.sobei.denuncias.model.entity.InscricaoCongresso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InscricaoCongressoRepository extends JpaRepository<InscricaoCongresso, Integer> {

    List<InscricaoCongresso> findAllByOrderByNomeCompletoAsc();

    Optional<InscricaoCongresso> findByCpf(String cpf);

    Optional<InscricaoCongresso> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM InscricaoCongresso i WHERE " +
           "i.cpf = :cpf OR REPLACE(REPLACE(i.cpf, '.', ''), '-', '') = :cpfSemMascara")
    boolean existsByCpfOrCpfSemMascara(
            @Param("cpf") String cpf,
            @Param("cpfSemMascara") String cpfSemMascara
    );

    @Query("SELECT i FROM InscricaoCongresso i WHERE " +
           "(i.cpf = :cpf OR REPLACE(REPLACE(i.cpf, '.', ''), '-', '') = :cpfSemMascara) AND " +
           "LOWER(TRIM(i.email)) = LOWER(TRIM(:email))")
    Optional<InscricaoCongresso> findByCpfAndEmail(
            @Param("cpf") String cpf,
            @Param("cpfSemMascara") String cpfSemMascara,
            @Param("email") String email
    );

    @Query("SELECT i FROM InscricaoCongresso i WHERE " +
           "(:termo IS NULL OR LOWER(i.nomeCompleto) LIKE LOWER(CONCAT('%', :termo, '%')) OR LOWER(i.email) LIKE LOWER(CONCAT('%', :termo, '%')) OR i.cpf LIKE CONCAT('%', :termo, '%') OR (:termoCpf IS NOT NULL AND REPLACE(REPLACE(i.cpf, '.', ''), '-', '') LIKE CONCAT('%', :termoCpf, '%'))) AND " +
           "(:unidade IS NULL OR LOWER(TRIM(i.unidade)) = LOWER(TRIM(:unidade))) AND " +
           "(:tipoOsc IS NULL OR LOWER(TRIM(i.tipoOsc)) = LOWER(TRIM(:tipoOsc))) AND " +
           "(:presente IS NULL OR i.presente = :presente) " +
           "ORDER BY i.nomeCompleto ASC")
    List<InscricaoCongresso> buscarComFiltros(
            @Param("termo") String termo,
            @Param("termoCpf") String termoCpf,
            @Param("unidade") String unidade,
            @Param("tipoOsc") String tipoOsc,
            @Param("presente") Boolean presente
    );

    @Query(value = "SELECT i FROM InscricaoCongresso i WHERE " +
           "(:termo IS NULL OR LOWER(i.nomeCompleto) LIKE LOWER(CONCAT('%', :termo, '%')) OR LOWER(i.email) LIKE LOWER(CONCAT('%', :termo, '%')) OR i.cpf LIKE CONCAT('%', :termo, '%') OR (:termoCpf IS NOT NULL AND REPLACE(REPLACE(i.cpf, '.', ''), '-', '') LIKE CONCAT('%', :termoCpf, '%'))) AND " +
           "(:unidade IS NULL OR LOWER(TRIM(i.unidade)) = LOWER(TRIM(:unidade))) AND " +
           "(:tipoOsc IS NULL OR LOWER(TRIM(i.tipoOsc)) = LOWER(TRIM(:tipoOsc))) AND " +
           "(:statusPresenca IS NULL OR " +
           " (:statusPresenca = 'dia11_presente' AND i.presenteDia11 = true) OR " +
           " (:statusPresenca = 'dia12_presente' AND i.presenteDia12 = true) OR " +
           " (:statusPresenca = 'ambos_presente' AND i.presenteDia11 = true AND i.presenteDia12 = true) OR " +
           " (:statusPresenca = 'nenhum_presente' AND (i.presenteDia11 = false OR i.presenteDia11 IS NULL) AND (i.presenteDia12 = false OR i.presenteDia12 IS NULL)) OR " +
           " (:statusPresenca = 'presente' AND i.presente = true) OR " +
           " (:statusPresenca = 'ausente' AND (i.presente = false OR i.presente IS NULL))) AND " +
           "(:presente IS NULL OR i.presente = :presente) " +
           "ORDER BY i.nomeCompleto ASC",
           countQuery = "SELECT COUNT(i) FROM InscricaoCongresso i WHERE " +
           "(:termo IS NULL OR LOWER(i.nomeCompleto) LIKE LOWER(CONCAT('%', :termo, '%')) OR LOWER(i.email) LIKE LOWER(CONCAT('%', :termo, '%')) OR i.cpf LIKE CONCAT('%', :termo, '%') OR (:termoCpf IS NOT NULL AND REPLACE(REPLACE(i.cpf, '.', ''), '-', '') LIKE CONCAT('%', :termoCpf, '%'))) AND " +
           "(:unidade IS NULL OR LOWER(TRIM(i.unidade)) = LOWER(TRIM(:unidade))) AND " +
           "(:tipoOsc IS NULL OR LOWER(TRIM(i.tipoOsc)) = LOWER(TRIM(:tipoOsc))) AND " +
           "(:statusPresenca IS NULL OR " +
           " (:statusPresenca = 'dia11_presente' AND i.presenteDia11 = true) OR " +
           " (:statusPresenca = 'dia12_presente' AND i.presenteDia12 = true) OR " +
           " (:statusPresenca = 'ambos_presente' AND i.presenteDia11 = true AND i.presenteDia12 = true) OR " +
           " (:statusPresenca = 'nenhum_presente' AND (i.presenteDia11 = false OR i.presenteDia11 IS NULL) AND (i.presenteDia12 = false OR i.presenteDia12 IS NULL)) OR " +
           " (:statusPresenca = 'presente' AND i.presente = true) OR " +
           " (:statusPresenca = 'ausente' AND (i.presente = false OR i.presente IS NULL))) AND " +
           "(:presente IS NULL OR i.presente = :presente)")
    Page<InscricaoCongresso> buscarComFiltrosPaginado(
            @Param("termo") String termo,
            @Param("termoCpf") String termoCpf,
            @Param("unidade") String unidade,
            @Param("tipoOsc") String tipoOsc,
            @Param("statusPresenca") String statusPresenca,
            @Param("presente") Boolean presente,
            Pageable pageable
    );

    @Query("SELECT i FROM InscricaoCongresso i WHERE " +
           "LOWER(TRIM(i.tipoOsc)) = 'sobei' AND LOWER(TRIM(i.unidade)) = LOWER(TRIM(:unidade)) AND " +
           "(:termo IS NULL OR LOWER(i.nomeCompleto) LIKE LOWER(CONCAT('%', :termo, '%')) OR LOWER(i.email) LIKE LOWER(CONCAT('%', :termo, '%')) OR i.cpf LIKE CONCAT('%', :termo, '%') OR (:termoCpf IS NOT NULL AND REPLACE(REPLACE(i.cpf, '.', ''), '-', '') LIKE CONCAT('%', :termoCpf, '%'))) AND " +
           "(:presente IS NULL OR i.presente = :presente) " +
           "ORDER BY i.nomeCompleto ASC")
    List<InscricaoCongresso> buscarPorUnidadeCoordenadora(
            @Param("unidade") String unidade,
            @Param("termo") String termo,
            @Param("termoCpf") String termoCpf,
            @Param("presente") Boolean presente
    );

    @Query(value = "SELECT i FROM InscricaoCongresso i WHERE " +
           "LOWER(TRIM(i.tipoOsc)) = 'sobei' AND LOWER(TRIM(i.unidade)) = LOWER(TRIM(:unidade)) AND " +
           "(:termo IS NULL OR LOWER(i.nomeCompleto) LIKE LOWER(CONCAT('%', :termo, '%')) OR LOWER(i.email) LIKE LOWER(CONCAT('%', :termo, '%')) OR i.cpf LIKE CONCAT('%', :termo, '%') OR (:termoCpf IS NOT NULL AND REPLACE(REPLACE(i.cpf, '.', ''), '-', '') LIKE CONCAT('%', :termoCpf, '%'))) AND " +
           "(:statusPresenca IS NULL OR " +
           " (:statusPresenca = 'dia11_presente' AND i.presenteDia11 = true) OR " +
           " (:statusPresenca = 'dia12_presente' AND i.presenteDia12 = true) OR " +
           " (:statusPresenca = 'ambos_presente' AND i.presenteDia11 = true AND i.presenteDia12 = true) OR " +
           " (:statusPresenca = 'nenhum_presente' AND (i.presenteDia11 = false OR i.presenteDia11 IS NULL) AND (i.presenteDia12 = false OR i.presenteDia12 IS NULL)) OR " +
           " (:statusPresenca = 'presente' AND i.presente = true) OR " +
           " (:statusPresenca = 'ausente' AND (i.presente = false OR i.presente IS NULL))) AND " +
           "(:presente IS NULL OR i.presente = :presente) " +
           "ORDER BY i.nomeCompleto ASC",
           countQuery = "SELECT COUNT(i) FROM InscricaoCongresso i WHERE " +
           "LOWER(TRIM(i.tipoOsc)) = 'sobei' AND LOWER(TRIM(i.unidade)) = LOWER(TRIM(:unidade)) AND " +
           "(:termo IS NULL OR LOWER(i.nomeCompleto) LIKE LOWER(CONCAT('%', :termo, '%')) OR LOWER(i.email) LIKE LOWER(CONCAT('%', :termo, '%')) OR i.cpf LIKE CONCAT('%', :termo, '%') OR (:termoCpf IS NOT NULL AND REPLACE(REPLACE(i.cpf, '.', ''), '-', '') LIKE CONCAT('%', :termoCpf, '%'))) AND " +
           "(:statusPresenca IS NULL OR " +
           " (:statusPresenca = 'dia11_presente' AND i.presenteDia11 = true) OR " +
           " (:statusPresenca = 'dia12_presente' AND i.presenteDia12 = true) OR " +
           " (:statusPresenca = 'ambos_presente' AND i.presenteDia11 = true AND i.presenteDia12 = true) OR " +
           " (:statusPresenca = 'nenhum_presente' AND (i.presenteDia11 = false OR i.presenteDia11 IS NULL) AND (i.presenteDia12 = false OR i.presenteDia12 IS NULL)) OR " +
           " (:statusPresenca = 'presente' AND i.presente = true) OR " +
           " (:statusPresenca = 'ausente' AND (i.presente = false OR i.presente IS NULL))) AND " +
           "(:presente IS NULL OR i.presente = :presente)")
    Page<InscricaoCongresso> buscarPorUnidadeCoordenadoraPaginado(
            @Param("unidade") String unidade,
            @Param("termo") String termo,
            @Param("termoCpf") String termoCpf,
            @Param("statusPresenca") String statusPresenca,
            @Param("presente") Boolean presente,
            Pageable pageable
    );

    @Query("SELECT i FROM InscricaoCongresso i WHERE LOWER(TRIM(i.tipoOsc)) = 'sobei' AND i.id <> :excludeId AND (i.oficina IS NOT NULL OR i.oficinaManha IS NOT NULL OR i.oficinaTarde IS NOT NULL)")
    List<InscricaoCongresso> findSobeiComOficinaExcludingId(@Param("excludeId") Integer excludeId);

    @Query("SELECT i FROM InscricaoCongresso i WHERE LOWER(TRIM(i.tipoOsc)) <> 'sobei' AND i.id <> :excludeId AND (i.oficina IS NOT NULL OR i.oficinaManha IS NOT NULL OR i.oficinaTarde IS NOT NULL)")
    List<InscricaoCongresso> findOutrasOscComOficinaExcludingId(@Param("excludeId") Integer excludeId);

    @Query("SELECT i FROM InscricaoCongresso i WHERE i.presenteDia11 = true AND i.presenteDia12 = true ORDER BY i.nomeCompleto ASC")
    List<InscricaoCongresso> findAllComCheckinAmbosDias();

    @Query("SELECT i FROM InscricaoCongresso i WHERE LOWER(TRIM(i.tipoOsc)) = 'sobei' AND LOWER(TRIM(i.unidade)) = LOWER(TRIM(:unidade)) AND i.presenteDia11 = true AND i.presenteDia12 = true ORDER BY i.nomeCompleto ASC")
    List<InscricaoCongresso> findAllComCheckinAmbosDiasPorUnidade(@Param("unidade") String unidade);
}
