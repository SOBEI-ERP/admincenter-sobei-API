package br.org.sobei.denuncias.repository;

import br.org.sobei.denuncias.model.entity.HistoricoEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoEstadoRepository extends JpaRepository<HistoricoEstado, Integer> {
    List<HistoricoEstado> findByDenunciaIdOrderByDataAlteracaoAsc(Integer denunciaId);
}
