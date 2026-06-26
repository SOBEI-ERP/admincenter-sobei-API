package br.org.sobei.denuncias.repository;

import br.org.sobei.denuncias.model.entity.Vaga;
import br.org.sobei.denuncias.model.enums.StatusVaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VagaRepository extends JpaRepository<Vaga, Integer> {

    List<Vaga> findByUnidadeOrderByDataCriacaoDesc(String unidade);

    List<Vaga> findByUnidadeAndStatusOrderByDataCriacaoDesc(String unidade, StatusVaga status);

    List<Vaga> findByStatusOrderByDataCriacaoDesc(StatusVaga status);

    List<Vaga> findAllByOrderByDataCriacaoDesc();

    boolean existsByUnidadeAndTituloAndStatusNot(String unidade, String titulo, StatusVaga status);

    boolean existsByUnidadeAndTituloAndStatusNotAndIdNot(String unidade, String titulo, StatusVaga status, Integer id);
}
