package br.org.sobei.denuncias.repository;

import br.org.sobei.denuncias.model.entity.MensagemUnidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensagemUnidadeRepository extends JpaRepository<MensagemUnidade, Integer> {

    List<MensagemUnidade> findByUnidadeOrderByDataEnvioDesc(String unidade);

    List<MensagemUnidade> findByUnidadeContainingIgnoreCaseOrderByDataEnvioDesc(String unidade);

    List<MensagemUnidade> findByUnidadeAndLidaOrderByDataEnvioDesc(String unidade, Boolean lida);

    List<MensagemUnidade> findByUnidadeContainingIgnoreCaseAndLidaOrderByDataEnvioDesc(String unidade, Boolean lida);

    List<MensagemUnidade> findAllByOrderByDataEnvioDesc();

    List<MensagemUnidade> findByLidaOrderByDataEnvioDesc(Boolean lida);

    long countByUnidadeAndLidaFalse(String unidade);

    long countByLidaFalse();
}
