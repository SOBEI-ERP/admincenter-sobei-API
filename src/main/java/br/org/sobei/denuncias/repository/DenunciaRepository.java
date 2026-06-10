package br.org.sobei.denuncias.repository;

import br.org.sobei.denuncias.model.entity.Denuncia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DenunciaRepository extends JpaRepository<Denuncia, Integer> {
    Optional<Denuncia> findByProtocolo(String protocolo);
}
