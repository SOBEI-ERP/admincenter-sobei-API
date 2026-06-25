package br.org.sobei.denuncias.repository;

import br.org.sobei.denuncias.model.entity.Candidatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidaturaRepository extends JpaRepository<Candidatura, Integer> {

    List<Candidatura> findByVagaIdOrderByDataEnvioDesc(Integer vagaId);

    long countByVagaId(Integer vagaId);
}
