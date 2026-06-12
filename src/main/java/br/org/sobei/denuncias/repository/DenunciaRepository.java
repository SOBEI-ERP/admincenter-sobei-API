package br.org.sobei.denuncias.repository;

import br.org.sobei.denuncias.model.entity.Denuncia;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DenunciaRepository extends JpaRepository<Denuncia, Integer>, JpaSpecificationExecutor<Denuncia> {

    @Override
    @EntityGraph(attributePaths = {"conclusao", "historicos"})
    List<Denuncia> findAll(Specification<Denuncia> spec, Sort sort);

    @EntityGraph(attributePaths = {"denunciante", "conclusao", "historicos"})
    Optional<Denuncia> findByProtocolo(String protocolo);
}
