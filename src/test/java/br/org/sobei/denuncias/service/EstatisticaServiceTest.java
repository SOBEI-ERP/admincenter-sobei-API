package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.response.EstatisticaResponse;
import br.org.sobei.denuncias.model.entity.Denuncia;
import br.org.sobei.denuncias.model.enums.StatusDenuncia;
import br.org.sobei.denuncias.model.enums.TipoDenuncia;
import br.org.sobei.denuncias.repository.DenunciaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstatisticaServiceTest {

    @Mock
    private DenunciaRepository denunciaRepository;

    @InjectMocks
    private EstatisticaService estatisticaService;

    @Test
    void testObterEstatisticasVazio() {
        when(denunciaRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        EstatisticaResponse response = estatisticaService.obterEstatisticas(null, null, null, null);

        assertNotNull(response);
        assertTrue(response.getPorUnidade().isEmpty());
        assertTrue(response.getDistribuicao().getTipos().isEmpty());
        assertTrue(response.getDistribuicao().getStatus().isEmpty());
    }

    @Test
    void testObterEstatisticasComDados() {
        Denuncia d1 = Denuncia.builder()
                .unidade("Leblon")
                .tipo(TipoDenuncia.ANONIMA)
                .estado(StatusDenuncia.NA_FILA)
                .build();
        Denuncia d2 = Denuncia.builder()
                .unidade("Leblon")
                .tipo(TipoDenuncia.IDENTIFICADA)
                .estado(StatusDenuncia.EM_ANDAMENTO)
                .build();

        when(denunciaRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList(d1, d2));

        EstatisticaResponse response = estatisticaService.obterEstatisticas(null, null, null, null);

        assertNotNull(response);
        assertEquals(2L, response.getPorUnidade().get("Leblon"));
        
        // Verifica se distribuições de tipo e status estão corretas
        assertEquals(1L, response.getDistribuicao().getTipos().stream()
                .filter(t -> t.getName().equals("ANONIMA")).findFirst().get().getValue());
        assertEquals(1L, response.getDistribuicao().getStatus().stream()
                .filter(s -> s.getName().equals("NA_FILA")).findFirst().get().getValue());
    }
}
