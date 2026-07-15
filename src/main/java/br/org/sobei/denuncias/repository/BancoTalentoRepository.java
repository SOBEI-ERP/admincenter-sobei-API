package br.org.sobei.denuncias.repository;

import br.org.sobei.denuncias.model.entity.BancoTalento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BancoTalentoRepository extends JpaRepository<BancoTalento, Integer> {

    List<BancoTalento> findByVagaIdOrderByDataEnvioOriginalDesc(Integer vagaId);

    List<BancoTalento> findByVagaUnidadeOrderByDataMovimentacaoDesc(String unidade);

    long countByVagaId(Integer vagaId);

    List<BancoTalento> findByDataMovimentacaoBefore(LocalDateTime limite);
}
