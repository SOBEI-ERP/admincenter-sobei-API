package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.request.AtualizarDenunciaRequest;
import br.org.sobei.denuncias.dto.response.DenunciaDetalheResponse;
import br.org.sobei.denuncias.model.entity.Denuncia;
import br.org.sobei.denuncias.model.enums.StatusDenuncia;
import br.org.sobei.denuncias.model.enums.TipoConclusao;
import br.org.sobei.denuncias.repository.ConclusaoDenunciaRepository;
import br.org.sobei.denuncias.repository.DenunciaRepository;
import br.org.sobei.denuncias.repository.HistoricoEstadoRepository;
import br.org.sobei.denuncias.repository.MedidaAdotadaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DenunciaAdminServiceTest {

    @Mock
    private DenunciaRepository denunciaRepository;
    @Mock
    private MedidaAdotadaRepository medidaAdotadaRepository;
    @Mock
    private HistoricoEstadoRepository historicoEstadoRepository;
    @Mock
    private ConclusaoDenunciaRepository conclusaoDenunciaRepository;

    @InjectMocks
    private DenunciaAdminService denunciaAdminService;

    @Test
    void testAtualizarDenunciaFecharSemRelatorioThrowsException() {
        Denuncia denuncia = Denuncia.builder().id(1).protocolo("XYZ-123-456").estado(StatusDenuncia.NA_FILA).build();
        when(denunciaRepository.findByProtocolo("XYZ-123-456")).thenReturn(Optional.of(denuncia));

        AtualizarDenunciaRequest request = new AtualizarDenunciaRequest();
        request.setStatus(StatusDenuncia.FECHADA);
        request.setRelatorio(null);

        assertThrows(IllegalArgumentException.class, () -> {
            denunciaAdminService.atualizarDenuncia("XYZ-123-456", request);
        });
    }

    @Test
    void testAtualizarDenunciaSucesso() {
        Denuncia denuncia = Denuncia.builder().id(1).protocolo("XYZ-123-456").estado(StatusDenuncia.NA_FILA).build();
        when(denunciaRepository.findByProtocolo("XYZ-123-456")).thenReturn(Optional.of(denuncia));

        AtualizarDenunciaRequest request = new AtualizarDenunciaRequest();
        request.setStatus(StatusDenuncia.FECHADA);
        request.setRelatorio("Relatorio concluido com sucesso.");
        request.setTipoConclusao(TipoConclusao.FINAL);

        DenunciaDetalheResponse response = denunciaAdminService.atualizarDenuncia("XYZ-123-456", request);

        assertNotNull(response);
        verify(conclusaoDenunciaRepository, times(1)).save(any());
        verify(historicoEstadoRepository, times(1)).save(any());
        verify(denunciaRepository, times(1)).save(any());
    }

}
