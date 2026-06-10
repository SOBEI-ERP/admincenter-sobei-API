package br.org.sobei.denuncias.repository;

import br.org.sobei.denuncias.model.entity.ConclusaoDenuncia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConclusaoDenunciaRepository extends JpaRepository<ConclusaoDenuncia, Integer> {
}
