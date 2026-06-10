package br.org.sobei.denuncias.repository;

import br.org.sobei.denuncias.model.entity.Denuncia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DenunciaRepository extends JpaRepository<Denuncia, Integer>, JpaSpecificationExecutor<Denuncia> {
    Optional<Denuncia> findByProtocolo(String protocolo);
}
