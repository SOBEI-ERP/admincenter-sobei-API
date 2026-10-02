package br.org.sobei.denuncias.service;

import br.org.sobei.denuncias.dto.DenunciaStatsDto;
import br.org.sobei.denuncias.dto.response.EstatisticaResponse;
import br.org.sobei.denuncias.model.entity.Denuncia;
import br.org.sobei.denuncias.model.enums.StatusDenuncia;
import br.org.sobei.denuncias.model.enums.TipoDenuncia;
import br.org.sobei.denuncias.dto.response.EstatisticaCongressoResponse;
import br.org.sobei.denuncias.model.entity.InscricaoCongresso;
import br.org.sobei.denuncias.repository.InscricaoCongressoRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstatisticaServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private InscricaoCongressoRepository inscricaoRepository;

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

    @Test
    void testObterEstatisticasCongressoVazio() {
        when(inscricaoRepository.findAll()).thenReturn(Collections.emptyList());

        EstatisticaCongressoResponse res = estatisticaService.obterEstatisticasCongresso();

        assertNotNull(res);
        assertEquals(0, res.getTotalInscritos());
        assertEquals(900, res.getLimiteVagas());
        assertEquals(0.0, res.getPercentualPreenchimento());
        assertEquals(0, res.getTotalSobei());
        assertEquals(0, res.getTotalOutrasOsc());
        assertEquals(0, res.getTotalComOficina());
        assertEquals(0, res.getTotalSemOficina());
        assertTrue(res.getPorUnidade().isEmpty());
        assertEquals(23, res.getPorOficina().size()); // 23 oficinas mapeadas
        assertTrue(res.getPorOutraOsc().isEmpty());
        assertTrue(res.getEvolucaoInscricoes().isEmpty());
    }

    @Test
    void testObterEstatisticasCongressoComDados() {
        InscricaoCongresso i1 = InscricaoCongresso.builder()
                .id(1)
                .nomeCompleto("Maria da Silva")
                .tipoOsc("SOBEI")
                .unidade("Montanaro")
                .oficina("Quem dança seus males espanta!")
                .presente(true)
                .presenteDia11(true)
                .presenteDia12(true)
                .dataInscricao(LocalDateTime.of(2026, 8, 1, 10, 0))
                .build();

        InscricaoCongresso i2 = InscricaoCongresso.builder()
                .id(2)
                .nomeCompleto("João Santos")
                .tipoOsc("SOBEI")
                .unidade("Leblon")
                .oficina(null)
                .presente(true)
                .presenteDia11(true)
                .presenteDia12(false)
                .dataInscricao(LocalDateTime.of(2026, 8, 1, 14, 30))
                .build();

        InscricaoCongresso i3 = InscricaoCongresso.builder()
                .id(3)
                .nomeCompleto("Ana Oliveira")
                .tipoOsc("OUTRA")
                .outraOsc("Creche Amiga")
                .oficina("Quem dança seus males espanta!")
                .presente(false)
                .presenteDia11(false)
                .presenteDia12(false)
                .dataInscricao(LocalDateTime.of(2026, 8, 2, 9, 15))
                .build();

        when(inscricaoRepository.findAll()).thenReturn(Arrays.asList(i1, i2, i3));

        EstatisticaCongressoResponse res = estatisticaService.obterEstatisticasCongresso();

        assertNotNull(res);
        assertEquals(3, res.getTotalInscritos());
        assertEquals(2, res.getTotalSobei());
        assertEquals(1, res.getTotalOutrasOsc());
        assertEquals(2, res.getTotalComOficina());
        assertEquals(1, res.getTotalSemOficina());
        assertEquals(2, res.getPresentesGeral());
        assertEquals(2, res.getPresentesDia11());
        assertEquals(1, res.getPresentesDia12());
        assertEquals(1, res.getPresentesAmbosDias());

        // Valida Unidades
        assertEquals(2, res.getPorUnidade().size());

        // Valida Outras OSCs
        assertEquals(1, res.getPorOutraOsc().size());
        assertEquals("Creche Amiga", res.getPorOutraOsc().get(0).getNomeOsc());
        assertEquals(1, res.getPorOutraOsc().get(0).getTotalInscritos());

        // Valida Oficinas
        assertEquals(23, res.getPorOficina().size());
        var oficinaDanca = res.getPorOficina().stream()
                .filter(o -> o.getMinistrante().contains("Rodrigo"))
                .findFirst()
                .orElse(null);
        assertNotNull(oficinaDanca);
        assertEquals(2, oficinaDanca.getTotalInscritos());
        assertEquals(1, oficinaDanca.getInscritosSobei());
        assertEquals(1, oficinaDanca.getInscritosOutrasOsc());
        assertEquals(66, oficinaDanca.getCapacidadeSala());

        // Valida Evolução temporal (2 dias diferentes)
        assertEquals(2, res.getEvolucaoInscricoes().size());
        assertEquals(2, res.getEvolucaoInscricoes().get(0).getNoDia());
        assertEquals(2, res.getEvolucaoInscricoes().get(0).getAcumulado());
        assertEquals(1, res.getEvolucaoInscricoes().get(1).getNoDia());
        assertEquals(3, res.getEvolucaoInscricoes().get(1).getAcumulado());
    }

    @Test
    void testAgrupamentoOutrasOscsVariaveis() {
        InscricaoCongresso o1 = InscricaoCongresso.builder().id(10).tipoOsc("OUTRA").outraOsc("CT-Vidas").build();
        InscricaoCongresso o2 = InscricaoCongresso.builder().id(11).tipoOsc("OUTRA").outraOsc("CT Vidas").build();
        InscricaoCongresso o3 = InscricaoCongresso.builder().id(12).tipoOsc("OUTRA").outraOsc("CTVidas - Centro de Treinamento das Vidas").build();
        InscricaoCongresso o4 = InscricaoCongresso.builder().id(13).tipoOsc("OUTRA").outraOsc("Ct-vidas").build();
        InscricaoCongresso o5 = InscricaoCongresso.builder().id(14).tipoOsc("OUTRA").outraOsc("Instituto Sonho").build();

        when(inscricaoRepository.findAll()).thenReturn(Arrays.asList(o1, o2, o3, o4, o5));

        EstatisticaCongressoResponse res = estatisticaService.obterEstatisticasCongresso();

        assertNotNull(res);
        assertEquals(5, res.getTotalInscritos());
        assertEquals(5, res.getTotalOutrasOsc());

        // Deve unificar as 4 variações de CT-Vidas em 1 único grupo com 4 inscritos
        assertEquals(2, res.getPorOutraOsc().size());

        var ctVidas = res.getPorOutraOsc().stream()
                .filter(o -> o.getNomeOsc().toLowerCase().contains("vidas"))
                .findFirst()
                .orElse(null);

        assertNotNull(ctVidas);
        assertEquals(4, ctVidas.getTotalInscritos());
        assertEquals(80.0, ctVidas.getPercentualOutras());
    }
}

