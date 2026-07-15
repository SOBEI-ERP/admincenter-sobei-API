package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.model.entity.BancoTalento;
import br.org.sobei.denuncias.model.entity.Candidatura;
import br.org.sobei.denuncias.repository.BancoTalentoRepository;
import br.org.sobei.denuncias.repository.CandidaturaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentCleanupScheduler {

    private final CandidaturaRepository candidaturaRepository;
    private final BancoTalentoRepository bancoTalentoRepository;
    private final StorageService storageService;

    @Scheduled(cron = "${app.cleanup.cron:0 0 3 * * ?}")
    @Transactional
    public void limparDocumentosAntigos() {
        log.info("Iniciando a limpeza automática de documentos/candidaturas com mais de 2 meses...");
        LocalDateTime limite = LocalDateTime.now().minusMonths(2);
        
        // 1. Limpar candidaturas antigas
        List<Candidatura> candidaturasAntigas = candidaturaRepository.findByDataEnvioBefore(limite);
        if (!candidaturasAntigas.isEmpty()) {
            log.info("Encontradas {} candidaturas antigas para remoção.", candidaturasAntigas.size());
            for (Candidatura candidatura : candidaturasAntigas) {
                try {
                    // Remove o arquivo no Cloudflare R2
                    storageService.delete(candidatura.getCurriculoPath());
                    // Remove a candidatura no banco de dados
                    candidaturaRepository.delete(candidatura);
                    log.debug("Candidatura {} e currículo associado limpos.", candidatura.getId());
                } catch (Exception e) {
                    log.error("Erro ao limpar a candidatura {} do candidato {}: {}", 
                            candidatura.getId(), candidatura.getNomeCompleto(), e.getMessage(), e);
                }
            }
        } else {
            log.info("Nenhuma candidatura antiga encontrada para limpeza.");
        }

        // 2. Limpar banco de talentos antigo
        List<BancoTalento> talentosAntigos = bancoTalentoRepository.findByDataMovimentacaoBefore(limite);
        if (!talentosAntigos.isEmpty()) {
            log.info("Encontrados {} talentos antigos no banco de talentos para remoção.", talentosAntigos.size());
            for (BancoTalento talento : talentosAntigos) {
                try {
                    // Remove o arquivo no Cloudflare R2
                    storageService.delete(talento.getCurriculoPath());
                    // Remove o talento no banco de dados
                    bancoTalentoRepository.delete(talento);
                    log.debug("Talento {} e currículo associado limpos do banco de talentos.", talento.getId());
                } catch (Exception e) {
                    log.error("Erro ao limpar o talento {} do candidato {} do banco de talentos: {}", 
                            talento.getId(), talento.getNomeCompleto(), e.getMessage(), e);
                }
            }
        } else {
            log.info("Nenhum talento antigo encontrado no banco de talentos para limpeza.");
        }

        log.info("Limpeza automática de documentos concluída.");
    }
}
