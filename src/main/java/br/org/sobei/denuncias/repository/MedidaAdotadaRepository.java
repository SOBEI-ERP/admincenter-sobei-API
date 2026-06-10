package br.org.sobei.denuncias.repository;

import br.org.sobei.denuncias.model.entity.MedidaAdotada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedidaAdotadaRepository extends JpaRepository<MedidaAdotada, Integer> {
    List<MedidaAdotada> findByDenunciaIdOrderByDataRegistroAsc(Integer denunciaId);
}
