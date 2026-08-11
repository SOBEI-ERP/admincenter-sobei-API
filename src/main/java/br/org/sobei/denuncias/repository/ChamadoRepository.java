package br.org.sobei.denuncias.repository;

import br.org.sobei.denuncias.model.entity.Chamado;
import br.org.sobei.denuncias.model.enums.PrioridadeChamado;
import br.org.sobei.denuncias.model.enums.StatusChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Integer> {

    List<Chamado> findAllByOrderByDataCriacaoDesc();

    List<Chamado> findByStatusOrderByDataCriacaoDesc(StatusChamado status);

    List<Chamado> findByPrioridadeOrderByDataCriacaoDesc(PrioridadeChamado prioridade);

    List<Chamado> findByStatusAndPrioridadeOrderByDataCriacaoDesc(StatusChamado status, PrioridadeChamado prioridade);
}
