package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.model.entity.BancoTalento;
import br.org.sobei.denuncias.model.entity.Candidatura;
import br.org.sobei.denuncias.repository.BancoTalentoRepository;
import br.org.sobei.denuncias.repository.CandidaturaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentCleanupSchedulerTest {

    @Mock
    private CandidaturaRepository candidaturaRepository;

    @Mock
    private BancoTalentoRepository bancoTalentoRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private DocumentCleanupScheduler documentCleanupScheduler;

    @Test
    void testLimparDocumentosAntigosComCandidaturasETalentos() {
        Candidatura cand = Candidatura.builder()
                .id(1)
                .nomeCompleto("João Silva")
                .curriculoPath("curriculos/joao.pdf")
                .dataEnvio(LocalDateTime.now().minusMonths(3))
                .build();

        BancoTalento talento = BancoTalento.builder()
                .id(10)
                .nomeCompleto("Maria Souza")
                .curriculoPath("curriculos/maria.pdf")
                .dataMovimentacao(LocalDateTime.now().minusMonths(3))
                .build();

        when(candidaturaRepository.findByDataEnvioBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(cand));
        when(bancoTalentoRepository.findByDataMovimentacaoBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(talento));

        documentCleanupScheduler.limparDocumentosAntigos();

        verify(storageService, times(1)).delete("curriculos/joao.pdf");
        verify(candidaturaRepository, times(1)).delete(cand);

        verify(storageService, times(1)).delete("curriculos/maria.pdf");
        verify(bancoTalentoRepository, times(1)).delete(talento);
    }

    @Test
    void testLimparDocumentosAntigosSemNenhumRegistro() {
        when(candidaturaRepository.findByDataEnvioBefore(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(bancoTalentoRepository.findByDataMovimentacaoBefore(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        documentCleanupScheduler.limparDocumentosAntigos();

        verify(storageService, never()).delete(anyString());
        verify(candidaturaRepository, never()).delete(any(Candidatura.class));
        verify(bancoTalentoRepository, never()).delete(any(BancoTalento.class));
    }
}
