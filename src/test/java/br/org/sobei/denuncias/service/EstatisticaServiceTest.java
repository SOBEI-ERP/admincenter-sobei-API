package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.DenunciaStatsDto;
import br.org.sobei.denuncias.dto.response.EstatisticaResponse;
import br.org.sobei.denuncias.model.entity.Denuncia;
import br.org.sobei.denuncias.model.enums.StatusDenuncia;
import br.org.sobei.denuncias.model.enums.TipoDenuncia;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CompoundSelection;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
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
    private EntityManager entityManager;

    @InjectMocks
    private EstatisticaService estatisticaService;

    @Test
    void testObterEstatisticasVazio() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<DenunciaStatsDto> query = mock(CriteriaQuery.class);
        Root<Denuncia> root = mock(Root.class);
        TypedQuery<DenunciaStatsDto> typedQuery = mock(TypedQuery.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(DenunciaStatsDto.class)).thenReturn(query);
        when(query.from(Denuncia.class)).thenReturn(root);
        when(cb.construct(eq(DenunciaStatsDto.class), any(), any(), any(), any())).thenReturn(mock(CompoundSelection.class));
        when(entityManager.createQuery(query)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        EstatisticaResponse response = estatisticaService.obterEstatisticas(null, null, null, null);

        assertNotNull(response);
        assertTrue(response.getPorUnidade().isEmpty());
        assertTrue(response.getDistribuicao().getTipos().isEmpty());
        assertTrue(response.getDistribuicao().getStatus().isEmpty());
        assertTrue(response.getDistribuicao().getPrioridades().isEmpty());
    }

    @Test
    void testObterEstatisticasComDados() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<DenunciaStatsDto> query = mock(CriteriaQuery.class);
        Root<Denuncia> root = mock(Root.class);
        TypedQuery<DenunciaStatsDto> typedQuery = mock(TypedQuery.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(DenunciaStatsDto.class)).thenReturn(query);
        when(query.from(Denuncia.class)).thenReturn(root);
        when(cb.construct(eq(DenunciaStatsDto.class), any(), any(), any(), any())).thenReturn(mock(CompoundSelection.class));
        when(entityManager.createQuery(query)).thenReturn(typedQuery);

        DenunciaStatsDto d1 = new DenunciaStatsDto("Leblon", TipoDenuncia.ANONIMA, StatusDenuncia.NA_FILA, br.org.sobei.denuncias.model.enums.PrioridadeDenuncia.NEUTRA);
        DenunciaStatsDto d2 = new DenunciaStatsDto("Leblon", TipoDenuncia.IDENTIFICADA, StatusDenuncia.EM_ANDAMENTO, br.org.sobei.denuncias.model.enums.PrioridadeDenuncia.ALTA);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(d1, d2));

        EstatisticaResponse response = estatisticaService.obterEstatisticas(null, null, null, null);

        assertNotNull(response);
        assertEquals(2L, response.getPorUnidade().get("Leblon"));
        
        // Verifica se distribuições de tipo, status e prioridades estão corretas
        assertEquals(1L, response.getDistribuicao().getTipos().stream()
                .filter(t -> t.getName().equals("ANONIMA")).findFirst().get().getValue());
        assertEquals(1L, response.getDistribuicao().getStatus().stream()
                .filter(s -> s.getName().equals("NA_FILA")).findFirst().get().getValue());
        assertEquals(1L, response.getDistribuicao().getPrioridades().stream()
                .filter(p -> p.getName().equals("NEUTRA")).findFirst().get().getValue());
        assertEquals(1L, response.getDistribuicao().getPrioridades().stream()
                .filter(p -> p.getName().equals("ALTA")).findFirst().get().getValue());
    }
}
